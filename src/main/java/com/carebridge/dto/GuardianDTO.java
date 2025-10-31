package dto;

import java.util.List;

public class GuardianDTO {
    private int id;
    private String name;
    private List<Integer> residentIds;

    public GuardianDTO() {}

    public GuardianDTO(int id, String name, List<Integer> residentIds) {
        this.id = id;
        this.name = name;
        this.residentIds = residentIds;
    }

    // Getters + Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<Integer> getResidentIds() { return residentIds; }
    public void setResidentIds(List<Integer> residentIds) { this.residentIds = residentIds; }
}
