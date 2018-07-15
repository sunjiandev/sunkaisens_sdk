package demo.skdroid.sunkaisens.com.demo.bean;

import java.util.List;

/**
 * 作者:sjy
 * 邮箱:sunjianyun@sunkaisens.com
 * 时间:2018/2/2 10:44
 */

public class GroupInfor {

    /**
     * displayName : 测试组18
     * groupLevel : 1
     * groupType : 1
     * max : 1
     * parentId :
     * useType : 1
     * groupEntrys : [{"uri":"sip:123@test.com","displayName":"458"}]
     */

    private String displayName;
    private int groupLevel;
    private String groupType;
    private int max;
    private String parentId;
    private int useType;
    private List<GroupEntrysBean> groupEntrys;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getGroupLevel() {
        return groupLevel;
    }

    public void setGroupLevel(int groupLevel) {
        this.groupLevel = groupLevel;
    }

    public String getGroupType() {
        return groupType;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public int getUseType() {
        return useType;
    }

    public void setUseType(int useType) {
        this.useType = useType;
    }

    public List<GroupEntrysBean> getGroupEntrys() {
        return groupEntrys;
    }

    public void setGroupEntrys(List<GroupEntrysBean> groupEntrys) {
        this.groupEntrys = groupEntrys;
    }

    public static class GroupEntrysBean {
        /**
         * uri : sip:123@test.com
         * displayName : 458
         */

        private String uri;
        private String displayName;

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }
    }
}
