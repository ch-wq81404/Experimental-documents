package com.bean.UML;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "actionSequence"
})
@XmlRootElement(name = "State.exit", namespace = "omg.org/UML1.3")
public class UMLStateExit {
	 @XmlElement(name = "ActionSequence", required = true, namespace = "omg.org/UML1.3")
	 protected UMLActionSequence actionSequence;
	 
	 public UMLActionSequence getUMLActionSequence() {
	        return actionSequence;
	    }

	    public void setGuard(UMLActionSequence value) {
	        this.actionSequence = value;
	    }
}
