package net.ajmichael.classfile;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.nio.ByteBuffer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ClassFileParser implements Function<ByteBuffer, ClassFile> {
  @Override
  public ClassFile apply(ByteBuffer classFile) {
    if (classFile.getInt() != ClassFile.magic()) {
      throw new IllegalArgumentException("Attempting to parse an invalid class file");
    }
    ClassFile.Builder builder = ClassFile.builder()
        .setMinorVersion(classFile.getShort())
        .setMajorVersion(classFile.getShort());
    short constantPoolCount = classFile.getShort();
    builder.setConstantPoolCount(constantPoolCount)
        .setConstantPool(applyN(constantPoolCount, ClassFileParser::readConstantPoolInfo, classFile))
        .setAccessFlags(classFile.getShort())
        .setThisClass(classFile.getShort())
        .setSuperClass(classFile.getShort());
    short interfacesCount = classFile.getShort();
    builder.setInterfacesCount(interfacesCount)
        .setInterfaces(applyN(interfacesCount, ByteBuffer::getShort, classFile));
    short fieldsCount = classFile.getShort();
    builder.setFieldsCount(fieldsCount)
        .setFields(applyN(fieldsCount, ClassFileParser::readFieldInfo, classFile));
    short methodsCount = classFile.getShort();
    builder.setMethodsCount(methodsCount)
        .setMethods(applyN(methodsCount, ClassFileParser::readMethodInfo, classFile));
    short attributesCount = classFile.getShort();
    return builder.setAttributesCount(attributesCount)
        .setAttributes(applyN(attributesCount, ClassFileParser::readAttributeInfo, classFile))
        .build();
  }

  private static <A, B> ImmutableList<B> applyN(int count, Function<A, B> function, A arg) {
    return IntStream.range(0, count)
        .mapToObj(x -> function.apply(arg))
        .collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableList::copyOf));
  }

  private static final ImmutableMap<Integer, Function<ByteBuffer, ConstantPoolInfo>> CONSTANT_POOL_PARSERS =
      new ImmutableMap.Builder<Integer, Function<ByteBuffer, ConstantPoolInfo>>()
          .put(7, ClassFileParser::readClassInfo)
          .put(9, ClassFileParser::readFieldRefInfo)
          .put(10, ClassFileParser::readMethodRefInfo)
          .put(11, ClassFileParser::readInterfaceMethodRefInfo)
          .put(8, ClassFileParser::readStringInfo)
          .put(3, ClassFileParser::readIntegerInfo)
          .put(4, ClassFileParser::readFloatInfo)
          .put(5, ClassFileParser::readLongInfo)
          .put(6, ClassFileParser::readDoubleInfo)
          .put(12, ClassFileParser::readNameAndTypeInfo)
          .put(1, ClassFileParser::readUtf8Info)
          .put(15, ClassFileParser::readMethodHandleInfo)
          .put(16, ClassFileParser::readMethodTypeInfo)
          .put(18, ClassFileParser::readInvokeDynamicInfo)
          .build();

  private static ConstantPoolInfo readConstantPoolInfo(ByteBuffer classFile) {
    return CONSTANT_POOL_PARSERS.get(classFile.get()).apply(classFile);
  }

  private static FieldInfo readFieldInfo(ByteBuffer classFile) {
    FieldInfo.Builder builder = FieldInfo.builder()
        .setAccessFlags(classFile.getShort())
        .setNameIndex(classFile.getShort())
        .setDescriptorIndex(classFile.getShort());
    short attributesCount = classFile.getShort();
    return builder
        .setAttributesCount(attributesCount)
        .setAttributes(applyN(attributesCount, ClassFileParser::readAttributeInfo, classFile))
        .build();
  }

  private static MethodInfo readMethodInfo(ByteBuffer classFile) {
    MethodInfo.Builder builder = MethodInfo.builder()
        .setAccessFlags(classFile.getShort())
        .setNameIndex(classFile.getShort())
        .setDescriptorIndex(classFile.getShort());
    short attributesCount = classFile.getShort();
    return builder.setAttributesCount(attributesCount)
        .setAttributes(applyN(attributesCount, ClassFileParser::readAttributeInfo, classFile))
        .build();
  }

  private static AttributeInfo readAttributeInfo(ByteBuffer classFile) {
    AttributeInfo.Builder builder = AttributeInfo.builder()
        .setAttributeNameIndex(classFile.getShort());
    int attributeLength = classFile.getInt();
    return builder.setAttributeLength(attributeLength)
        .setInfo(applyN(attributeLength, ByteBuffer::get, classFile))
        .build();
  }

  private static ClassInfo readClassInfo(ByteBuffer classFile) {
    return ClassInfo.builder()
        .setNameIndex(classFile.getShort())
        .build();
  }

  private static FieldRefInfo readFieldRefInfo(ByteBuffer classFile) {
    return FieldRefInfo.builder()
        .setClassIndex(classFile.getShort())
        .setNameAndTypeIndex(classFile.getShort())
        .build();
  }

  private static MethodRefInfo readMethodRefInfo(ByteBuffer classFile) {
    return MethodRefInfo.builder()
        .setClassIndex(classFile.getShort())
        .setNameAndTypeIndex(classFile.getShort())
        .build();
  }

  private static InterfaceMethodRefInfo readInterfaceMethodRefInfo(ByteBuffer classFile) {
    return InterfaceMethodRefInfo.builder()
        .setClassIndex(classFile.getShort())
        .setNameAndTypeIndex(classFile.getShort())
        .build();
  }

  private static StringInfo readStringInfo(ByteBuffer classFile) {
    return StringInfo.builder()
        .setStringIndex(classFile.getShort())
        .build();
  }

  private static IntegerInfo readIntegerInfo(ByteBuffer classFile) {
    return IntegerInfo.builder()
        .setBytes(classFile.getInt())
        .build();
  }

  private static FloatInfo readFloatInfo(ByteBuffer classFile) {
    return FloatInfo.builder()
        .setBytes(classFile.getInt())
        .build();
  }

  private static LongInfo readLongInfo(ByteBuffer classFile) {
    return LongInfo.builder()
        .setHighBytes(classFile.getInt())
        .setLowBytes(classFile.getInt())
        .build();
  }

  private static DoubleInfo readDoubleInfo(ByteBuffer classFile) {
    return DoubleInfo.builder()
        .setHighBytes(classFile.getInt())
        .setLowBytes(classFile.getInt())
        .build();
  }

  private static NameAndTypeInfo readNameAndTypeInfo(ByteBuffer classFile) {
    return NameAndTypeInfo.builder()
        .setNameIndex(classFile.getShort())
        .setDescriptorIndex(classFile.getShort())
        .build();
  }

  private static Utf8Info readUtf8Info(ByteBuffer classFile) {
    short length = classFile.getShort();
    return Utf8Info.builder()
        .setLength(length)
        .setBytes(applyN(length, ByteBuffer::get, classFile))
        .build();
  }

  private static MethodHandleInfo readMethodHandleInfo(ByteBuffer classFile) {
    return MethodHandleInfo.builder()
        .setReferenceKind(classFile.get())
        .setReferenceIndex(classFile.getShort())
        .build();
  }

  private static MethodTypeInfo readMethodTypeInfo(ByteBuffer classFile) {
    return MethodTypeInfo.builder()
        .setDescriptorIndex(classFile.getShort())
        .build();
  }

  private static InvokeDynamicInfo readInvokeDynamicInfo(ByteBuffer classFile) {
    return InvokeDynamicInfo.builder()
        .setBootstrapMethodAttrIndex(classFile.getShort())
        .setNameAndTypeIndex(classFile.getShort())
        .build();
  }
}