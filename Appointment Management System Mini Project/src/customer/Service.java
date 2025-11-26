package customer;

public class Service {
    private String serviceName;
    private String providerName;
    private int maxCustomers;
    private String description;

    public Service(String serviceName, String providerName, int maxCustomers, String description) {
        this.serviceName = serviceName;
        this.providerName = providerName;
        this.maxCustomers = maxCustomers;
        this.description = description;
    }

    public String getServiceName() { return serviceName; }
    public String getProviderName() { return providerName; }
    public int getMaxCustomers() { return maxCustomers; }
    public String getDescription() { return description; }
}
