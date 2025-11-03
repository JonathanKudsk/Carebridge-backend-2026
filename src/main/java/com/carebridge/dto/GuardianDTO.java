package dto;
import java.util.List;

public class GuardianDTO {
    private int id;
    private String name;
    private List<int> residentIds;

    public GuardianDTO() {}

    public GuardianDTO(int id, String name, List<int> residentIds) {
        this.id = id;
        this.name = name;
        this.residentIds = residentIds;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<int> getResidentIds() { return residentIds; }
    public void setResidentIds(List<int> residentIds) { this.residentIds = residentIds; }
}
