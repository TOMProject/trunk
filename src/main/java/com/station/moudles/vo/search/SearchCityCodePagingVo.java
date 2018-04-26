package com.station.moudles.vo.search;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * This class was generated by Bill Generator.
 * This class corresponds to the database table city_code  
 *
 * @zdmgenerated 2017-13-19 07:13
 */
@ApiModel(value="CityCode查询",description="CityCode查询描述")
public class SearchCityCodePagingVo extends PageEntity {
    /**
     * This field corresponds to the database column city_code.id  
     */
    @ApiModelProperty(value="pk",required=false)
    private Integer id;

    /**
     * This field corresponds to the database column city_code.city_code  城市区域编码
     */
    @ApiModelProperty(value="城市区域编码",example="城市区域编码",required=false)
    private String cityCode;

    /**
     * This field corresponds to the database column city_code.city_name  城市区域名称
     */
    @ApiModelProperty(value="城市区域名称",example="城市区域名称",required=false)
    private String cityName;

    /**
     * This field corresponds to the database column city_code.parent_code  城市区域父级编码
     */
    @ApiModelProperty(value="城市区域父级编码",example="城市区域父级编码",required=false)
    private String parentCode;

    /**
     * This field corresponds to the database column city_code.type  城市类型：1--国家；2--省份；3--市级；4--区县
     */
    @ApiModelProperty(value="城市类型：1--国家；2--省份；3--市级；4--区县",required=false)
    private Integer type;

    /**
     * This method returns the value of the database column city_code.id  
     * @return the value of city_code.id
     */
    public Integer getId() {
        return id;
    }

    /**
     * This method sets the value of the database column city_code.id  
     * @param id the value for city_code.id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * This method returns the value of the database column city_code.city_code  城市区域编码
     * @return the value of city_code.city_code
     */
    public String getCityCode() {
        return cityCode;
    }

    /**
     * This method sets the value of the database column city_code.city_code  城市区域编码
     * @param cityCode the value for city_code.city_code
     */
    public void setCityCode(String cityCode) {
        this.cityCode = cityCode == null ? null : cityCode.trim();
    }

    /**
     * This method returns the value of the database column city_code.city_name  城市区域名称
     * @return the value of city_code.city_name
     */
    public String getCityName() {
        return cityName;
    }

    /**
     * This method sets the value of the database column city_code.city_name  城市区域名称
     * @param cityName the value for city_code.city_name
     */
    public void setCityName(String cityName) {
        this.cityName = cityName == null ? null : cityName.trim();
    }

    /**
     * This method returns the value of the database column city_code.parent_code  城市区域父级编码
     * @return the value of city_code.parent_code
     */
    public String getParentCode() {
        return parentCode;
    }

    /**
     * This method sets the value of the database column city_code.parent_code  城市区域父级编码
     * @param parentCode the value for city_code.parent_code
     */
    public void setParentCode(String parentCode) {
        this.parentCode = parentCode == null ? null : parentCode.trim();
    }

    /**
     * This method returns the value of the database column city_code.type  城市类型：1--国家；2--省份；3--市级；4--区县
     * @return the value of city_code.type
     */
    public Integer getType() {
        return type;
    }

    /**
     * This method sets the value of the database column city_code.type  城市类型：1--国家；2--省份；3--市级；4--区县
     * @param type the value for city_code.type
     */
    public void setType(Integer type) {
        this.type = type;
    }
}