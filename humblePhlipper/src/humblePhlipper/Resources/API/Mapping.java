package humblePhlipper.resources.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Mapping {

    @SerializedName("examine")
    @Expose
    private String examine;
    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("members")
    @Expose
    private Boolean members;
    @SerializedName("lowalch")
    @Expose
    private Integer lowalch;
    @SerializedName("limit")
    @Expose
    private Integer limit;
    @SerializedName("value")
    @Expose
    private Integer value;
    @SerializedName("highalch")
    @Expose
    private Integer highalch;
    @SerializedName("icon")
    @Expose
    private String icon;
    @SerializedName("name")
    @Expose
    private String name;

    public String getExamine() {
        return examine;
    }

    public void setExamine(String examine) {
        this.examine = examine;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean getMembers() {
        return members;
    }

    public void setMembers(Boolean members) {
        this.members = members;
    }

    public Integer getLowalch() {
        return lowalch;
    }

    public void setLowalch(Integer lowalch) {
        this.lowalch = lowalch;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public Integer getHighalch() {
        return highalch;
    }

    public void setHighalch(Integer highalch) {
        this.highalch = highalch;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
