package com.station.common.utils;

/**
 * Active Set method for quadratic programming with inequality constraints.
 *
 * This class solves the quadratic programming problem
 *     min f(x)=1/2*x^T*H*x+c^T*x
 *     s.t. Ax>=b
 * with the Active Set method.
 *
 * Reference
 * [1] 最优化理论与算法 (第2版), 陈宝林, 清华大学出版社
 */
public class ActiveSet {
    private int max_iteration;
    private double gradient_tol, constraint_tol;

    /**
     * Default initialization
     */
    public ActiveSet() {
        this(1000, 1e-6, 1e-6);
    }

    /**
     * Initialize the solver
     *
     * @param max_iteration maximum iteration count. If set to zero or a negative value, then there will be no iteration count limit.
     * @param gradient_tol gradient tolerance. If the norm of the gradient is less than this value, the gradient is considered to be zero.
     * @param constraint_tol constraint tolerance. If |A(i)x-b(i)| is less than this value, it is considered to be zero.
     *
     * @throws IllegalArgumentException if gradient_tol < 0 or constraint_tol < 0
     */
    public ActiveSet(int max_iteration, double gradient_tol, double constraint_tol) {
        if (gradient_tol < 0) {
            throw new IllegalArgumentException("Gradient tolerance must not be negative");
        }
        if (constraint_tol < 0) {
            throw new IllegalArgumentException("Constraint tolerance must not be negative");
        }
        this.max_iteration = max_iteration;
        this.gradient_tol = gradient_tol;
        this.constraint_tol = constraint_tol;
    }

    /**
     * Private methods. All parameters are unchecked for validity
     */
    private static void fill(double m[], double val) {
        for (int i = 0; i < m.length; ++i) {
            m[i] = val;
        }
    }

    private static double index(double mat[], int m, int n, int row, int col) {
        return mat[m * col + row];
    }

    private static void assign(double mat[], int m, int n, int row, int col, double val) {
        mat[m * col + row] = val;
    }

    private static void assign(double m1[], double m2[]) {
        for (int i = 0; i < m1.length; ++i) {
            m1[i] = m2[i];
        }
    }

    private static void increment(double m1[], double m2[]) {
        for (int i = 0; i < m1.length; ++i) {
            m1[i] += m2[i];
        }
    }

    private static void increment(double m1[], double m2[], double v) {
        for (int i = 0; i < m1.length; ++i) {
            m1[i] += m2[i] * v;
        }
    }

    private static void decrement(double mat[], int m, int n, int row, int col, double val) {
        mat[m * col + row] -= val;
    }

    private static void subtract(double m1[], double m2[], double m3[]) {
        for (int i = 0; i < m1.length; ++i) {
            m1[i] = m2[i] - m3[i];
        }
    }

    private static void multiply(double r[], double a[], double b[], int m, int n, int p) {
        for (int i = 0; i < m; ++i) {
            for (int k = 0; k < p; ++k) {
                double s = 0;
                for (int j = 0; j < n; ++j) {
                    s += index(a, m, n, i, j) * index(b, n, p, j, k);
                }
                assign(r, m, p, i, k, s);
            }
        }
    }

    private static double dot(double v1[], double v2[]) {
        double d = 0;
        for (int i = 0; i < v1.length; ++i) {
            d += v1[i] * v2[i];
        }
        return d;
    }

    private static void gradient(double g[], double H[], double c[], double x[]) {
        multiply(g, x, H, 1, x.length, x.length);
        increment(g, c);
    }

    private static void chol(double L[], double D[], double A[], int dim) {
        fill(L, 0);
        fill(D, 0);
        for (int i = 0; i < dim; ++i) {
            assign(L, dim, dim, i, i, 1);
            for (int j = 0; j < i; ++j) {
                double Lij = index(A, dim, dim, i, j);
                for (int k = 0; k < j; ++k) {
                    Lij -= index(L, dim, dim, i, k) * index(L, dim, dim, j, k) * index(D, dim, dim, k, k);
                }
                Lij /= index(D, dim, dim, j, j);
                assign(L, dim, dim, i, j, Lij);
            }
            double Di = index(A, dim, dim, i, i);
            for (int k = 0; k < i; ++k) {
                double Lik = index(L, dim, dim, i, k);
                Di -= Lik * Lik * index(D, dim, dim, k, k);
            }
            assign(D, dim, dim, i, i, Di);
        }
    }

    private static void low_unitri_inv(double inv[], double tri[], int dim) {
        fill(inv, 0);
        for (int k = 0; k < dim; ++k) {
            assign(inv, dim, dim, k, k, 1);
        }
        for (int k = 0; k < dim; ++k) {
            for (int i = k + 1; i < dim; ++i) {
                for (int j = 0; j <= k; ++j) {
                    decrement(inv, dim, dim, i, j, index(tri, dim, dim, i, k) * index(inv, dim, dim, k, j));
                }
            }
        }
    }

    private static void pos_def_inv(double inv[], double mat[], int dim) {
        double L[] = new double[dim * dim], D[] = new double[dim * dim];
        chol(L, D, mat, dim);
        double invL[] = new double[dim * dim];
        low_unitri_inv(invL, L, dim);
        for (int i = 0; i < dim; ++i) {
            for (int k = 0; k < dim; ++k) {
                double s = 0;
                for (int j = 0; j < dim; ++j) {
                    s += index(invL, dim, dim, j, i) * index(invL, dim, dim, j, k) / index(D, dim, dim, j, j);
                }
                assign(inv, dim, dim, i, k, s);
            }
        }
    }

    /**
     * Solve the quadratic programming problem
     *     min f(x)=1/2*x^T*H*x+c^T*x
     *     s.t. Ax>=b
     *
     * @param H should contain c.length * c.length row-major elements, and should be positive-definite. The solver
     * will not check for H's positive-definiteness;
     * @param c a vector
     * @param A should contain b.length * c.length row-major elements, and rank(A) should equal b.length. The solver
     * will not check for A's rank.
     * @param b a vector
     * @param x0 an feasible initial solution. Its length should equal the length of c.
     *
     * @return the solution vector with length equal to c.length, or null if no solution is found
     *
     * @throws IllegalArgumentException if input dimensions do not match, or if the initial solution is not feasible
     */
    public double[] solve(double H[], double c[], double A[], double b[], double x0[]) {
        if (H.length != c.length * c.length) {
            throw new IllegalArgumentException("The number of elements in H must equal c.length * c.length");
        }
        if (A.length != b.length * c.length) {
            throw new IllegalArgumentException("The number of elements in A must equal b.length * c.length");
        }
        if (x0.length != c.length) {
            throw new IllegalArgumentException("The length of x0 must equal the length of c");
        }
        if (c.length == 0) {
            return null;
        }
        double invH[] = new double[H.length];
        double x[] = new double[c.length];
        double Ax[] = new double[b.length];
        boolean active[] = new boolean[b.length];
        int active_cnt = 0;
        double AI[] = new double[A.length];
        double AIT[] = new double[A.length];
        int max_length = Math.max(b.length, c.length);
        double tmp1[] = new double[max_length * max_length];
        double tmp2[] = new double[max_length * max_length];
        double R[] = new double[A.length];
        double Q[] = new double[H.length];
        double g[] = new double[c.length];
        double d[] = new double[c.length];
        double lambda[] = new double[b.length];
        double Ad[] = new double[b.length];
        pos_def_inv(invH, H, c.length);
        // Given the initial solution, find all active constraints
        assign(x, x0);
        multiply(Ax, A, x, b.length, c.length, 1);
        for (int i = 0; i < b.length; ++i) {
            if (Ax[i] < b[i] - constraint_tol) {
                throw new IllegalArgumentException("The initial solution is not feasible");
            } else if (Ax[i] <= b[i] + constraint_tol) {
                active[i] = true;
                ++active_cnt;
            } else {
                active[i] = false;
            }
        }
        for (int iter = 0; max_iteration < 0 || iter < max_iteration; ++iter) {
            // Solve the subproblem with the active equality constraints
            gradient(g, H, c, x);
            if (dot(g, g) <= gradient_tol * gradient_tol) {
                return x;
            }
            if (active_cnt == 0) {
                multiply(d, invH, g, c.length, c.length, 1);
            } else {
                for (int i = 0, j = 0; i < b.length; ++i) {
                    if (active[i]) {
                        for (int k = 0; k < c.length; ++k) {
                            double v = index(A, b.length, c.length, i, k);
                            assign(AI, active_cnt, c.length, j, k, v);
                            assign(AIT, c.length, active_cnt, k, j, v);
                        }
                        ++j;
                    }
                }
                multiply(tmp1, AI, invH, active_cnt, c.length, c.length);       // AI*H^-1
                multiply(tmp2, tmp1, AIT, active_cnt, c.length, active_cnt);    // AI*H^-1*AI^T
                pos_def_inv(tmp1, tmp2, active_cnt);                            // (AI*H^-1*AI^T)^-1
                multiply(tmp2, tmp1, AI, active_cnt, active_cnt, c.length);     // (AI*H^-1*AI^T)^-1*AI
                multiply(R, tmp2, invH, active_cnt, c.length, c.length);        // (AI*H^-1*AI^T)^-1*AI*H^-1
                multiply(tmp1, AIT, R, c.length, active_cnt, c.length);         // AI^T*(AI*H^-1*AI^T)^-1*AI*H^-1
                multiply(tmp2, invH, tmp1, c.length, c.length, c.length);       // H^-1*AI^T*(AI*H^-1*AI^T)^-1*AI*H^-1
                subtract(Q, invH, tmp2);                                        // H^-1-H^-1*AI^T*(AI*H^-1*AI^T)^-1*AI*H^-1
                multiply(d, Q, g, c.length, c.length, 1);                       // Note: this value is negative to the one in ref [1]
            }
            // Calculate optimal step size with the inactive constraints
            double alpha = -1;      // Note: since d is negative to d in ref [1], alpha & all related calculations are negated
            int alpha_ind = -1;
            multiply(Ad, A, d, b.length, c.length, 1);
            for (int i = 0; i < b.length; ++i) {
                if (!active[i] && Ad[i] > 0) {
                    double v = (b[i] - Ax[i]) / Ad[i];
                    if (v > alpha) {
                        alpha = v;
                        alpha_ind = i;
                    }
                }
            }
            increment(x, d, alpha);
            if (alpha_ind >= 0) {
                // Add the new active constraint
                active[alpha_ind] = true;
                ++active_cnt;
            } else if (active_cnt > 0) {
                // Remove the non-optimal active constraint
                multiply(lambda, R, g, active_cnt, c.length, 1);
                double min_lambda = 0;
                int min_lambda_ind = -1;
                for (int i = 0, j = 0; i < b.length; ++i) {
                    if (active[i]) {
                        if (lambda[j] < min_lambda) {
                            min_lambda = lambda[j];
                            min_lambda_ind = i;
                        }
                        ++j;
                    }
                }
                if (min_lambda_ind >= 0) {
                    active[min_lambda_ind] = false;
                    --active_cnt;
                } else {
                    return x;
                }
            } else {
                return x;
            }
            multiply(Ax, A, x, b.length, c.length, 1);
        }
        return x;
    }
}
