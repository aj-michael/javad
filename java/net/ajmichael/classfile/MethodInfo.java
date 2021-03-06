package net.ajmichael.classfile;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

import java.nio.ByteBuffer;

import static net.ajmichael.util.Helpers.applyN;

@AutoValue
public abstract class MethodInfo {
  public static MethodInfo parse(ByteBuffer classFile) {
    MethodInfo.Builder builder = MethodInfo.builder()
        .setAccessFlags(classFile.getShort())
        .setNameIndex(classFile.getShort())
        .setDescriptorIndex(classFile.getShort());
    short attributesCount = classFile.getShort();
    return builder.setAttributesCount(attributesCount)
        .setAttributes(applyN(attributesCount, AttributeInfo::parse, classFile))
        .build();
  }

  public static Builder builder() {
    return new AutoValue_MethodInfo.Builder();
  }

  public abstract Short accessFlags();

  public abstract Short nameIndex();

  public abstract Short descriptorIndex();

  public abstract Short attributesCount();

  public abstract ImmutableList<AttributeInfo> attributes();

  @AutoValue.Builder
  public static abstract class Builder {
    public abstract MethodInfo build();

    public abstract Builder setAccessFlags(Short value);

    public abstract Builder setNameIndex(Short value);

    public abstract Builder setDescriptorIndex(Short value);

    public abstract Builder setAttributesCount(Short value);

    public abstract Builder setAttributes(ImmutableList<AttributeInfo> value);
  }
}
