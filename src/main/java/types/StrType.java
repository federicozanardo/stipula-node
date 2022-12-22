package types;

public class StrType extends Type {
  final private String type = "str";
  private String value;

  public StrType() {
    this.value = "";
  }

  public StrType(String value) {
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
