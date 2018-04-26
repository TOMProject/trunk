package com.station.moudles.entity;

import java.math.BigDecimal;

public class SuggestCellInfo {
	//单体编号
	private Integer cellIndex;
	//内阻值平均值
	private BigDecimal cellResist;
	// 截止电压
	private BigDecimal endVol;
	// 容量
	private BigDecimal cellCap;
	
	public Integer getCellIndex() {
		return cellIndex;
	}
	public void setCellIndex(Integer cellIndex) {
		this.cellIndex = cellIndex;
	}
	public BigDecimal getCellResist() {
		return cellResist;
	}
	public void setCellResist(BigDecimal cellResist) {
		this.cellResist = cellResist;
	}
	public BigDecimal getEndVol() {
		return endVol;
	}
	public void setEndVol(BigDecimal endVol) {
		this.endVol = endVol;
	}
	public BigDecimal getCellCap() {
		return cellCap;
	}
	public void setCellCap(BigDecimal cellCap) {
		this.cellCap = cellCap;
	}
}
