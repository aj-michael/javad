package net.ajmichael.javad;

import net.ajmichael.classfile.ClassFile;
import net.ajmichael.classfile.ClassFileParser;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

public class App {
  public static void main(String[] args) throws IOException {
    ClassFile classFile = new ClassFileParser()
        .apply(ByteBuffer.wrap(Files.readAllBytes(Paths.get(args[0]))));
    System.out.println(classFile);
  }
}