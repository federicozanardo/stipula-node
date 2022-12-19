package types;

public class StringType extends Type {
  final private String type = "str";
  private String value;

  public StringType() {
    this.value = "";
  }

  public StringType(String value) {
    this.value = value;
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public String getValue() {
    return value;
  }
}
