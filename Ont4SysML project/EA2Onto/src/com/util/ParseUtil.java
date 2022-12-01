package com.util;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.bean.UML.XMI;

import java.io.*;

public class ParseUtil {

    public static XMI parseXMI(File file) {
        JAXBContext context;
        try {
            context = JAXBContext.newInstance(XMI.class);
            Unmarshaller um = context.createUnmarshaller();
            XMI xmi = (XMI) um.unmarshal(new FileInputStream(file));
            return xmi;
        } catch (JAXBException | FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

}
