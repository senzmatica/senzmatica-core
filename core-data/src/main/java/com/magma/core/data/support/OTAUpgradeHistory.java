package com.magma.core.data.support;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.magma.util.MagmaDateTimeDeserializer;
import com.magma.util.MagmaDateTimeSerializer;
import org.joda.time.DateTime;

public class OTAUpgradeHistory {
    private String actionBy;

    @JsonSerialize(using = MagmaDateTimeSerializer.class)
    @JsonDeserialize(using = MagmaDateTimeDeserializer.class)
    private DateTime updatedDate;

    private String currentVersion;

    private String previousVersion;

    public String getActionBy() {
        return actionBy;
    }

    public void setActionBy(String actionBy) {
        this.actionBy = actionBy;
    }

    public DateTime getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(DateTime updatedDate) {
        this.updatedDate = updatedDate;
    }

    public String getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(String currentVersion) {
        this.currentVersion = currentVersion;
    }

    public String getPreviousVersion() {
        return previousVersion;
    }

    public void setPreviousVersion(String previousVersion) {
        this.previousVersion = previousVersion;
    }
}
