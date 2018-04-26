package com.station.common.utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;

import com.station.moudles.entity.PackDataInfo;

public class ModelCalcHelper {
	private Date[] rcv_time;
	
	private double[] cell_vol_1;
	private double[] cell_vol_2;
	private double[] cell_vol_3;
	private double[] cell_vol_4;
	private double[] cell_vol_5;
	private double[] cell_vol_6;
	private double[] cell_vol_7;
	private double[] cell_vol_8;
	private double[] cell_vol_9;
	private double[] cell_vol_10;
	private double[] cell_vol_11;
	private double[] cell_vol_12;
	private double[] cell_vol_13;
	private double[] cell_vol_14;
	private double[] cell_vol_15;
	private double[] cell_vol_16;
	private double[] cell_vol_17;
	private double[] cell_vol_18;
	private double[] cell_vol_19;
	private double[] cell_vol_20;
	private double[] cell_vol_21;
	private double[] cell_vol_22;
	private double[] cell_vol_23;
	private double[] cell_vol_24;
	
	private double[] gen_vol;
	private double[] gen_cur;
	private String[] stete;

    public int cell_cnt;
    
    private int index;

	private double standardCap; // 电池组标称容量
    
    private ModelCalcHelper(List<PackDataInfo> dischargeList, int cellCount, double standardCap) {
    	this.cell_cnt = cellCount;
    	this.standardCap = standardCap;
    	initDatas(dischargeList);
    }
    
    public static ModelCalcHelper newInstance(List<PackDataInfo> dischargeList, int cellCount, double standardCap) {
    	return new ModelCalcHelper(dischargeList, cellCount, standardCap);
    }
    
    private void initDatas(List<PackDataInfo> dischargeList) {
    	rcv_time = new Date[dischargeList.size()];
    	gen_vol = dischargeList.stream().mapToDouble(p -> p.getGenVol().doubleValue()).toArray();
    	gen_cur = dischargeList.stream().mapToDouble(p -> p.getGenCur().doubleValue()).toArray();
    	dischargeList.stream().map(p -> p.getRcvTime())
					    	.collect(Collectors.toList())
					    	.toArray(rcv_time);
    	
    	for (int j = 1; j < cell_cnt + 1; j++) {
    		index = j;
    		double[] array = dischargeList.stream().mapToDouble(p -> {
    				Object cellVol = ReflectUtil.getValueByKey(p, "cellVol" + index);
    				return Double.parseDouble(cellVol.toString());
    			}).toArray();
    		ReflectUtil.setValueByKet(this, "cell_vol_" + index, array);
    	}
	}
	
    private static double sqr(double x) {
        return x * x;
    }

    private static double sigmoid(double x, double mu, double sigma) {
        double norm = (x - mu) / sigma;
        return 1. / (1 + Math.exp(-norm));
    }

    /**
     * Estimate the start and end point of discharge.
     * NOTE: the algorithm is not perfect, and will fail to extract the discharge interval on unusual data inputs.
     * The returned start point denotes the last sample before discharge begins,
     * and the end point denotes the last point before discharge ends.
     *
     * @return two-element array containing the start index and end index of discharge
     */
    public int[] estimateDischargeStartAndEnd() {
        double y[] = gen_vol;
        double i[] = gen_cur;
        if (y.length != i.length) {
            throw new IllegalArgumentException("Data must have the same length");
        }
        if (y.length <= 2) {
            int result[] = new int[2];
            result[0] = 0;
            result[1] = Math.max(0, y.length - 1);
            return result;
        }
        int len = y.length;
        double disc_cur_std = 0, delta_cur_std = 0, vol_mean = 0, vol_std = 0, delta_vol_std = 0, max_vol = 0;
        for (int k = 0; k < len; ++k) {
            if (i[k] < 0) {
                disc_cur_std += sqr(i[k]);
            }
            if (k > 0) {
                delta_cur_std += sqr(i[k] - i[k - 1]);
                delta_vol_std += sqr(y[k] - y[k - 1]);
                max_vol = Math.max(max_vol, y[k]);
            } else {
                max_vol = y[k];
            }
            vol_mean += y[k];
            vol_std += sqr(y[k]);
        }
        disc_cur_std = Math.sqrt(disc_cur_std / len);
        delta_cur_std = Math.sqrt(delta_cur_std / (len - 1));
        vol_mean /= len;
        vol_std = Math.sqrt(vol_std / len - sqr(vol_mean));
        delta_vol_std = Math.sqrt(delta_vol_std / (len - 1));
        double start_likelihood[] = new double[len];
        double end_likelihood[] = new double[len];
        for (int k = 0; k < len; ++k) {
            start_likelihood[k] = sigmoid(Math.min(0, i[k]), 0, disc_cur_std)                                           // i_k is positive
                * sigmoid(k > 0 ? Math.min(0, i[k] - i[k - 1]) : 0, 0, delta_cur_std * 2)                               // i_k - i_{k-1} is non-negative
                * sigmoid(k > 0 ? Math.min(0, y[k] - y[k - 1]) : 0, 0, delta_vol_std * 2)                               // y_k - y_{k-1} is non-negative
                * sigmoid(k < len - 1 ? i[k] - i[k + 1] : 0, 0, (delta_cur_std + disc_cur_std) * .5)                    // i_{k+1} - i_k is negative
                * sigmoid(k < len - 1 ? y[k] - y[k + 1] : 0, 0, (delta_vol_std + vol_std) * .5)                         // y_{k+1} - y_k is negative
                * sigmoid(y[k], vol_mean, vol_std)                                                                      // y_k is high
                * (sigmoid(k < len - 1 ? -Math.min(0, i[k + 1]) : -Math.min(0, i[k]), 0, disc_cur_std) - 0.5);          // i_{k+1} is negative
            end_likelihood[k] = (sigmoid(-Math.min(0, i[k]), 0, disc_cur_std) - 0.5)                                    // i_k is negative
                * sigmoid(k > 0 ? Math.min(0, i[k - 1] - i[k]) : 0, 0, delta_cur_std * 2)                               // i_k - i_{k-1} is non-positive
                * sigmoid(k > 0 ? Math.min(0, y[k - 1] - y[k]) : 0, 0, delta_vol_std * 2)                               // y_k - y_{k-1} is non-positive
                * sigmoid(k < len - 1 ? i[k + 1] - i[k] : -Math.min(0, i[k]), 0, (delta_cur_std + disc_cur_std) * .5)   // i_{k+1} - i_k is positive
                * sigmoid(k < len - 1 ? y[k + 1] - y[k] : max_vol - y[k], 0, (delta_vol_std + vol_std) * .5)            // y_{k+1} - y_k is positive
                * (1 - sigmoid(y[k], vol_mean, vol_std))                                                                // y_k is low
                * sigmoid(k < len - 1 ? Math.min(0, i[k + 1]) : 0, 0, disc_cur_std);                                    // i_{k+1} is positive
        }
        int max_end_likelihood_index = len - 1;
        double max_end_likelihood = end_likelihood[max_end_likelihood_index];
//        int max_likelihood_start = len - 2;
//        int max_likelihood_end = len - 1;
        int avg = len/2;
        int max_likelihood_start = len > 20 ? 9 : (avg - 1 < 0 ? 0 : avg - 1);
        int max_likelihood_end = len > 20? (len - 11) : avg;
        double max_interval_likelihood = start_likelihood[max_likelihood_start] * end_likelihood[max_likelihood_end];
        //优化结束及开始点查找。
        int likeend = Math.max(len/2, len - 20);//结束点的可能起始位置，要么在数据段的后半段，要么从数据结束位置倒数20个数据点。
        int likestart= Math.min(len/2, 20);//开始点的可能位置，要么在数据段的前半段，要么在前20个数据点中。
        for (int k = len - 3; k >= 0; --k) {
            if (end_likelihood[k + 1] > max_end_likelihood && ((end_likelihood[k + 1] > 0 && k > likeend) || max_end_likelihood == 0)) {
                max_end_likelihood = end_likelihood[k + 1];
                max_end_likelihood_index = k + 1;
            }
            if (start_likelihood[k] * max_end_likelihood > max_interval_likelihood && k < likestart) {
                max_interval_likelihood = start_likelihood[k] * max_end_likelihood;
                max_likelihood_start = k;
                max_likelihood_end = max_end_likelihood_index;
            }
        }
        int result[] = new int[2];
        result[0] = max_likelihood_start;
        result[1] = max_likelihood_end;
        return result;
    }
    
    private static double CoulombCounting(Date t[], double i[], int start, int end) {
        double ampere_millisec = 0;
        for (int k = start; k < end; ++k) {
            ampere_millisec += -i[k] * (t[k + 1].getTime() - t[k].getTime());
        }
        return ampere_millisec / 3600000d;
    }

    /**
     * Estimate pack capacity and individual cell capacity using the given start and end point of discharge.
     * NOTE: for cells that did not discharge below the cutoff voltage, the capacity is estimated assuming a
     * third-order polynomial relationship between the final voltage and the cell capacity. This model is not
     * verified, so the precision of such estimation should not be relied upon.
     *
     * @param disc_start index of the last sample before discharge starts
     * @param disc_end index of the last sample before discharge ends
     *
     * @return a double array, containing the pack capacity as its first value, and the individual cell capacity
     * as the following values.
     */
    public double[] estimateCellCapacity(int disc_start, int disc_end) {
        double capacity[] = new double[cell_cnt + 1];
        Date t[] = rcv_time;
        double i[] = gen_cur;
        int duration_start = Math.min(disc_start + 1, disc_end - 1);
        capacity[0] = CoulombCounting(t, i, duration_start, disc_end);
        double cutoff_voltage = 0, cutoff_volt_std = 0;
        for (int j = 0; j < cell_cnt; ++j) {
            double vol = getcell_vol(j + 1)[disc_end];
            cutoff_voltage += vol;
            cutoff_volt_std += sqr(vol);
        }
        cutoff_voltage /= cell_cnt;
        cutoff_volt_std = Math.sqrt(cutoff_volt_std / cell_cnt - sqr(cutoff_voltage));
        int filtered_cnt = 0;
        double filtered_cutoff = 0;
        for (int j = 0; j < cell_cnt; ++j) {
            double vol = getcell_vol(j + 1)[disc_end];
            if (vol >= cutoff_voltage - cutoff_volt_std * 3 && vol <= cutoff_voltage + cutoff_volt_std * 3) {
                filtered_cutoff += vol;
                ++filtered_cnt;
            }
        }
        if (filtered_cnt > 0) {
            filtered_cutoff /= filtered_cnt;
        } else {
            filtered_cutoff = cutoff_voltage;
        }
        double slant = 0.25;
        int known_cnt = 0;
        for (int j = 0; j < cell_cnt; ++j) {
            double yj[] = getcell_vol(j + 1);
            if (yj[disc_end] <= filtered_cutoff) {
                int cutoff_ind = duration_start;
                for (int k = disc_end; k >= duration_start; --k) {
                    if (yj[k] >= filtered_cutoff * ((double) (k - duration_start) / (disc_end - duration_start) * slant - slant + 1)) {
                        cutoff_ind = k;
                        break;
                    }
                }
                capacity[j + 1] = CoulombCounting(t, i, duration_start, cutoff_ind);
                ++known_cnt;
            } else {
                capacity[j + 1] = Double.NaN;
                
            }
        }
        double x[];
        if (known_cnt < 2) {
            x = null;
        } else {
        	double vol_Rep=Double.NaN;
        	int replace_num=0;
        	//if there is only 2 capacity values , add a capacity=0;
        	if(known_cnt==2) {
        		 for (int j = 0; j < cell_cnt; ++j) {
        			 double cap=capacity[j + 1];
        			  if (Double.isNaN(cap)) {
        				  vol_Rep= getcell_vol(j+1)[disc_end];
        				  getcell_vol(j+1)[disc_end]=filtered_cutoff;
        				  capacity[j + 1]=capacity[0];
        				  replace_num=j+1;
        				  known_cnt++;
        				  break;
        			  }
        		 }
        	}
        	
	    	/*double [] vol = new double[known_cnt];
	    	double [] cap = new double[known_cnt];
	    	int index = 0;
	    	for (int j = 0; j < cell_cnt; ++j) {
	    		double temp=capacity[j + 1];
	    		if (!Double.isNaN(temp)) {
	    			vol[index] = getcell_vol(j+1)[disc_end];
	    			cap[index] = capacity[j + 1];
	    			index++;
	    		}
		 	}
	    	double[] thirdFitting = thirdFitting(vol, cap);
	    	if (thirdFitting == null) {
	    		for (int j = 0; j < cell_cnt; ++j) {
	                if (Double.isNaN(capacity[j + 1])) {
	                    capacity[j + 1] = capacity[0];
	                }
	            }
			}else {
				for (int j = 0; j < cell_cnt; ++j) {
	                if (Double.isNaN(capacity[j + 1])) {
	                    double v1 = getcell_vol(j + 1)[disc_end];
	                    capacity[j + 1] = thirdFitting[3] * v1 * v1 * v1 + thirdFitting[2] * v1 * v1+ thirdFitting[1] * v1 + thirdFitting[0];
	                }
	            }
			}
        }*/
            // Assuming C_k = a*v_k^3 + b*v_k + c, identify the model using known final voltage and capacity values,
            // and then estimate the unknown capacity with the identified model
            double s1 = 0, s2 = 0, s3 = 0, s4 = 0, s6 = 0, sc0 = 0, sc1 = 0, sc3 = 0, vm1 = 0;
            for (int j = 0; j < cell_cnt; ++j) {
                double v1 = getcell_vol(j + 1)[disc_end];
                double cap = capacity[j + 1];
                if (!Double.isNaN(cap)) {
                    double v2 = v1 * v1;
                    double v3 = v2 * v1;
                    s1 += v1;
                    s2 += v2;
                    s3 += v3;
                    s4 += v2 * v2;
                    s6 += v3 * v3;
                    sc0 += cap;
                    sc1 += v1 * cap;
                    sc3 += v3 * cap;
                }
                vm1 = Math.max(vm1, v1);
            }
            double H[] = new double[9];
            H[0] = s6;
            H[1] = H[3] = s4;
            H[2] = H[6] = s3;
            H[4] = s2;
            H[5] = H[7] = s1;
            H[8] = known_cnt;
            double c[] = new double[3];
            c[0] = sc3;
            c[1] = sc1;
            c[2] = sc0;
            double A[] = new double[15];
            double co1 = filtered_cutoff, co3 = co1 * co1 * co1;
            double vm3 = vm1 * vm1 * vm1;
            A[0] = A[6] = A[12] = A[13] = 1;
            A[1] = A[2] = A[5] = A[7] = A[10] = A[11] = 0;
            A[3] = co3;
            A[4] = -vm3;
            A[8] = co1;
            A[9] = -vm1;
            A[14] = -1;
            double b[] = new double[5];
            b[0] = b[1] = b[2] = 0;
            b[3] = capacity[0];
            b[4] = capacity[0] * -2;
            x = new double[3];
            x[0] = 0;
            x[1] = 0;
            x[2] = capacity[0];
            ActiveSet solver = new ActiveSet();
            try {
                x = solver.solve(H, c, A, b, x);
            } catch (IllegalArgumentException ex) {
                x = null;
            }
            
            if (!Double.isNaN(vol_Rep)) {
            	getcell_vol(replace_num)[disc_end]=vol_Rep;
            	capacity[replace_num]=Double.NaN;
			}
        }
      
        if (x == null) {
            for (int j = 0; j < cell_cnt; ++j) {
                if (Double.isNaN(capacity[j + 1])) {
                    capacity[j + 1] = capacity[0];
                }
            }
        } else {
            for (int j = 0; j < cell_cnt; ++j) {
                if (Double.isNaN(capacity[j + 1])) {
                    double v1 = getcell_vol(j + 1)[disc_end];
                    capacity[j + 1] = (x[0] * v1 * v1 + x[1]) * v1 + x[2];
                }
            }
        }
        capacity = capAbnormalHandler(capacity, disc_end);
        return capacity;
    }
    
    private double[] capAbnormalHandler(double[] capacity, int disc_end) {
		double[] fixedCap = new double[capacity.length];
		System.arraycopy(capacity, 0, fixedCap, 0, capacity.length);
		
    	double maxCap = Math.min(fixedCap[0]*1.5, standardCap*1.01);
    	
    	List<DataPoint> points = new ArrayList<>();
		for (int j = 1; j < cell_cnt + 1; j++) {
			if (fixedCap[j] <= maxCap && fixedCap[j] > 0) {
				points.add(new DataPoint(getcell_vol(j)[disc_end], fixedCap[j]));
			}
		}
		FittingLine ft = new FittingLine(points.stream().toArray(DataPoint[]::new));
    	for (int i = 1; i < cell_cnt + 1; i++) {
			if (Double.isNaN(fixedCap[i]) || fixedCap[i] > maxCap || fixedCap[i] < 0) {
				if (CollectionUtils.isNotEmpty(points)) {
					fixedCap[i] =  ft.at(getcell_vol(i)[disc_end]);
				}
				if (Double.isNaN(fixedCap[i]) || fixedCap[i] > maxCap) {
					fixedCap[i] = maxCap;
	            }
			}
		}
		return fixedCap;
	}

	public double[] estimateLower(double[] vol,double[] cap) {
    	double[] x=new double[2];
    	x[0]=(cap[1]-cap[0])/(vol[1]-vol[0]);
    	x[1]=cap[0]-x[0]*vol[0];
    	return x;
    }
	
	private double[] thirdFitting(double[] vol, double[] cap) {
		if (vol== null || cap == null || vol.length != cap.length || vol.length < 3) {
			return null;
		}
		WeightedObservedPoints obs = new WeightedObservedPoints();  
		for (int i = 0; i < cap.length; i++) {
			obs.add(vol[i], cap[i]);
		}
        // Instantiate a third-degree polynomial fitter.  
        PolynomialCurveFitter fitter = PolynomialCurveFitter.create(3);  
  
        // Retrieve fitted parameters (coefficients of the polynomial function).  
        return fitter.fit(obs.toList());  
	}

    public double[] getcell_vol(int ind) {
        switch (ind) {
        default:
            throw new IndexOutOfBoundsException("Cell index out of range");
        case 1:
            return getcell_vol_1();
        case 2:
            return getcell_vol_2();
        case 3:
            return getcell_vol_3();
        case 4:
            return getcell_vol_4();
        case 5:
            return getcell_vol_5();
        case 6:
            return getcell_vol_6();
        case 7:
            return getcell_vol_7();
        case 8:
            return getcell_vol_8();
        case 9:
            return getcell_vol_9();
        case 10:
            return getcell_vol_10();
        case 11:
            return getcell_vol_11();
        case 12:
            return getcell_vol_12();
        case 13:
            return getcell_vol_13();
        case 14:
            return getcell_vol_14();
        case 15:
            return getcell_vol_15();
        case 16:
            return getcell_vol_16();
        case 17:
            return getcell_vol_17();
        case 18:
            return getcell_vol_18();
        case 19:
            return getcell_vol_19();
        case 20:
            return getcell_vol_20();
        case 21:
            return getcell_vol_21();
        case 22:
            return getcell_vol_22();
        case 23:
            return getcell_vol_23();
        case 24:
            return getcell_vol_24();
        }
    }

    public double[] getgen_vol() {
		return gen_vol;
	}
	public void setgen_vol(double[] gen_vol) {
		this.gen_vol = gen_vol;
	}
	public double[] getgen_cur() {
		return gen_cur;
	}
	public void setgen_cur(double[] gen_cur) {
		this.gen_cur = gen_cur;
	}
	public String[] getstete() {
		return stete;
	}
	public void setstete(String[] stete) {
		this.stete = stete;
	}
	public Date[] getrcv_time() {
		return rcv_time;
	}
	public void setrcv_time(Date[] rcv_time) {
		this.rcv_time = rcv_time;
	}
	public double[] getcell_vol_1() {
		return cell_vol_1;
	}
	public void setcell_vol_1(double[] cell_vol_1) {
		this.cell_vol_1 = cell_vol_1;
	}
	public double[] getcell_vol_2() {
		return cell_vol_2;
	}
	public void setcell_vol_2(double[] cell_vol_2) {
		this.cell_vol_2 = cell_vol_2;
	}
	public double[] getcell_vol_3() {
		return cell_vol_3;
	}
	public void setcell_vol_3(double[] cell_vol_3) {
		this.cell_vol_3 = cell_vol_3;
	}
	public double[] getcell_vol_4() {
		return cell_vol_4;
	}
	public void setcell_vol_4(double[] cell_vol_4) {
		this.cell_vol_4 = cell_vol_4;
	}
	public double[] getcell_vol_5() {
		return cell_vol_5;
	}
	public void setcell_vol_5(double[] cell_vol_5) {
		this.cell_vol_5 = cell_vol_5;
	}
	public double[] getcell_vol_6() {
		return cell_vol_6;
	}
	public void setcell_vol_6(double[] cell_vol_6) {
		this.cell_vol_6 = cell_vol_6;
	}
	public double[] getcell_vol_7() {
		return cell_vol_7;
	}
	public void setcell_vol_7(double[] cell_vol_7) {
		this.cell_vol_7 = cell_vol_7;
	}
	public double[] getcell_vol_8() {
		return cell_vol_8;
	}
	public void setcell_vol_8(double[] cell_vol_8) {
		this.cell_vol_8 = cell_vol_8;
	}
	public double[] getcell_vol_9() {
		return cell_vol_9;
	}
	public void setcell_vol_9(double[] cell_vol_9) {
		this.cell_vol_9 = cell_vol_9;
	}
	public double[] getcell_vol_10() {
		return cell_vol_10;
	}
	public void setcell_vol_10(double[] cell_vol_10) {
		this.cell_vol_10 = cell_vol_10;
	}
	public double[] getcell_vol_11() {
		return cell_vol_11;
	}
	public void setcell_vol_11(double[] cell_vol_11) {
		this.cell_vol_11 = cell_vol_11;
	}
	public double[] getcell_vol_12() {
		return cell_vol_12;
	}
	public void setcell_vol_12(double[] cell_vol_12) {
		this.cell_vol_12 = cell_vol_12;
	}
	public double[] getcell_vol_13() {
		return cell_vol_13;
	}
	public void setcell_vol_13(double[] cell_vol_13) {
		this.cell_vol_13 = cell_vol_13;
	}
	public double[] getcell_vol_14() {
		return cell_vol_14;
	}
	public void setcell_vol_14(double[] cell_vol_14) {
		this.cell_vol_14 = cell_vol_14;
	}
	public double[] getcell_vol_15() {
		return cell_vol_15;
	}
	public void setcell_vol_15(double[] cell_vol_15) {
		this.cell_vol_15 = cell_vol_15;
	}
	public double[] getcell_vol_16() {
		return cell_vol_16;
	}
	public void setcell_vol_16(double[] cell_vol_16) {
		this.cell_vol_16 = cell_vol_16;
	}
	public double[] getcell_vol_17() {
		return cell_vol_17;
	}
	public void setcell_vol_17(double[] cell_vol_17) {
		this.cell_vol_17 = cell_vol_17;
	}
	public double[] getcell_vol_18() {
		return cell_vol_18;
	}
	public void setcell_vol_18(double[] cell_vol_18) {
		this.cell_vol_18 = cell_vol_18;
	}
	public double[] getcell_vol_19() {
		return cell_vol_19;
	}
	public void setcell_vol_19(double[] cell_vol_19) {
		this.cell_vol_19 = cell_vol_19;
	}
	public double[] getcell_vol_20() {
		return cell_vol_20;
	}
	public void setcell_vol_20(double[] cell_vol_20) {
		this.cell_vol_20 = cell_vol_20;
	}
	public double[] getcell_vol_21() {
		return cell_vol_21;
	}
	public void setcell_vol_21(double[] cell_vol_21) {
		this.cell_vol_21 = cell_vol_21;
	}
	public double[] getcell_vol_22() {
		return cell_vol_22;
	}
	public void setcell_vol_22(double[] cell_vol_22) {
		this.cell_vol_22 = cell_vol_22;
	}
	public double[] getcell_vol_23() {
		return cell_vol_23;
	}
	public void setcell_vol_23(double[] cell_vol_23) {
		this.cell_vol_23 = cell_vol_23;
	}
	public double[] getcell_vol_24() {
		return cell_vol_24;
	}
	public void setcell_vol_24(double[] cell_vol_24) {
		this.cell_vol_24 = cell_vol_24;
	}
}
