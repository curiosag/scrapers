package tmdn;

public class TmdRow {
    public  int id;

    public String indicationOfProduct;
    public String designNumber;
    public String st13;
    public String ownerName;
    public String ownerCountryCode;
    public String ownerAddress;
    public String representativeName;
    public String representativeCountryCode;
    public String representativeAddress;
    public String designOffice;
    public String designatedTerritory;
    public String locarnoClassification;
    public String status;
    public String applicationDate;
    public String registrationDate;
    public String publicationDate;
    public String expiryDate;
    public int pageNumber;
    public String urlOwnerDetails;
    public String urlRepresentativeDetails;

    public int sliceNumber;

    public TmdRow setId(int id) {
        this.id = id;
        return this;
    }

    public TmdRow setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
        return this;
    }

    public TmdRow setSliceNumber(int sliceNumber) {
        this.sliceNumber = sliceNumber;
        return this;
    }

    public TmdRow setRepresentativeCountryCode(String representativeCountryCode) {
        this.representativeCountryCode = representativeCountryCode;
        return this;
    }

    public TmdRow setRepresentativeAddress(String representativeAddress) {
        this.representativeAddress = representativeAddress;
        return this;
    }

    public TmdRow setOwnerAddress(String ownerAddress) {
        this.ownerAddress = ownerAddress;
        return this;
    }

    public TmdRow setIndicationOfProduct(String indicationOfProduct) {
        this.indicationOfProduct = indicationOfProduct;
        return this;
    }

    public TmdRow setDesignNumber(String designNumber) {
        this.designNumber = designNumber;
        return this;
    }

    public TmdRow setOwnerName(String ownerName) {
        this.ownerName = ownerName;
        return this;
    }

    public TmdRow setOwnerCountryCode(String ownerCountryCode) {
        this.ownerCountryCode = ownerCountryCode;
        return this;
    }

    public TmdRow setStatus(String status) {
        this.status = status;
        return this;
    }

    public TmdRow setDesignOffice(String designOffice) {
        this.designOffice = designOffice;
        return this;
    }

    public TmdRow setDesignatedTerritory(String designatedTerritory) {
        this.designatedTerritory = designatedTerritory;
        return this;
    }

    public TmdRow setLocarnoClassification(String locarnoClassification) {
        this.locarnoClassification = locarnoClassification;
        return this;
    }

    public TmdRow setRepresentativeName(String representativeName) {
        this.representativeName = representativeName;
        return this;
    }

    public TmdRow setSt13(String st13) {
        this.st13 = st13;
        return this;
    }

    public TmdRow setUrlOwnerDetails(String urlOwnerDetails) {
        this.urlOwnerDetails = urlOwnerDetails;
        return this;
    }

    public TmdRow setUrlRepresentativeDetails(String urlRepresentativeDetails) {
        this.urlRepresentativeDetails = urlRepresentativeDetails;
        return this;
    }

    public TmdRow setApplicationDate(String applicationDate) {
        this.applicationDate = applicationDate;
        return this;
    }

    public TmdRow setRegistrationDate(String registrationDate) {
        this.registrationDate = registrationDate;
        return this;
    }

    public TmdRow setPublicationDate(String publicationDate) {
        this.publicationDate = publicationDate;
        return this;
    }

    public TmdRow setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
        return this;
    }

    @Override
    public String toString() {
        return "TmdRow{" +
                "indicationOfProduct='" + indicationOfProduct + '\'' +
                ", designNumber='" + designNumber + '\'' +
                ", ownerName='" + ownerName + '\'' +
                ", ownerAddress='" + ownerAddress + '\'' +
                ", representativeName='" + representativeName + '\'' +
                ", representativeCountryCode='" + st13 + '\'' +
                ", representativeAddress='" + urlOwnerDetails + '\'' +
                ", status='" + status + '\'' +
                ", designOffice='" + designOffice + '\'' +
                ", designatedTerritory='" + designatedTerritory + '\'' +
                ", locarnoClassification='" + locarnoClassification + '\'' +
                ", applicationDate='" + applicationDate + '\'' +
                ", registrationDate='" + registrationDate + '\'' +
                ", publicationDate='" + publicationDate + '\'' +
                ", expiryDate='" + expiryDate + '\'' +
                '}';
    }
}
