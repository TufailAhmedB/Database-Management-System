package customer;

public class Company {
    private String ownerName, companyName, businessType, phone, email, website, workingTime, offDays, address;

    public Company(String ownerName, String companyName, String businessType,
                   String phone, String email, String website,
                   String workingTime, String offDays, String address) {

        this.ownerName = ownerName;
        this.companyName = companyName;
        this.businessType = businessType;
        this.phone = phone;
        this.email = email;
        this.website = website;
        this.workingTime = workingTime;
        this.offDays = offDays;
        this.address = address;
    }

    public String getOwnerName() { return ownerName; }
    public String getCompanyName() { return companyName; }
    public String getBusinessType() { return businessType; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getWebsite() { return website; }
    public String getWorkingTime() { return workingTime; }
    public String getOffDays() { return offDays; }
    public String getAddress() { return address; }
}
