package dev.osunolimits.models;

import java.util.List;

import lombok.Data;

@Data
public class UserInfoObject {
    public int id;
    public String name;
    public String safe_name;
    public int priv;
    public List<Group> groups;
}
