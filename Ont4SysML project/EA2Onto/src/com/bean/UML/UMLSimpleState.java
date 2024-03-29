//
// 此文件是由 JavaTM Architecture for XML Binding (JAXB) 引用实现 v2.2.8-b130911.1802 生成的
// 请访问 <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// 在重新编译源模式时, 对此文件的所有修改都将丢失。
// 生成时间: 2022.05.09 时间 03:54:03 PM CST 
//


package com.bean.UML;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * <p>anonymous complex type的 Java 类。
 * 
 * <p>以下模式片段指定包含在此类中的预期内容。
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{omg.org/UML1.3}ModelElement.taggedValue"/>
 *         &lt;choice minOccurs="0">
 *           &lt;element ref="{omg.org/UML1.3}State.exit"/>
 *           &lt;element ref="{omg.org/UML1.3}State.internalTransition"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="namespace" use="required" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="visibility" use="required" type="{http://www.w3.org/2001/XMLSchema}NCName" />
 *       &lt;attribute name="xmi.id" use="required" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "modelElementTaggedValue",
    "stateEntry",
    "stateExit",
    "stateInternalTransition"
})
@XmlRootElement(name = "SimpleState", namespace = "omg.org/UML1.3")
public class UMLSimpleState  implements UMLState {

    @XmlElement(name = "ModelElement.taggedValue", required = true, namespace = "omg.org/UML1.3")
    protected UMLModelElementTaggedValue modelElementTaggedValue;
    @XmlElement(name = "State.exit", namespace = "omg.org/UML1.3")
    protected UMLStateExit stateExit;
    @XmlElement(name = "State.entry", namespace = "omg.org/UML1.3")
    protected UMLStateEntry stateEntry;
    @XmlElement(name = "State.internalTransition", namespace = "omg.org/UML1.3")
    protected UMLStateInternalTransition stateInternalTransition;
    @XmlAttribute(name = "name", required = true)
    @XmlSchemaType(name = "anySimpleType")
    protected String name;
    @XmlAttribute(name = "namespace", required = true)
    @XmlSchemaType(name = "anySimpleType")
    protected String namespace;
    @XmlAttribute(name = "visibility", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    protected String visibility;
    @XmlAttribute(name = "xmi.id", required = true)
    @XmlSchemaType(name = "anySimpleType")
    protected String xmiId;

    /**
     * 获取modelElementTaggedValue属性的值。
     * 
     * @return
     *     possible object is
     *     {@link UMLModelElementTaggedValue }
     *     
     */
    public UMLModelElementTaggedValue getModelElementTaggedValue() {
        return modelElementTaggedValue;
    }

    /**
     * 设置modelElementTaggedValue属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link UMLModelElementTaggedValue }
     *     
     */
    public void setModelElementTaggedValue(UMLModelElementTaggedValue value) {
        this.modelElementTaggedValue = value;
    }

    /**
     * 获取stateExit属性的值。
     * 
     * @return
     *     possible object is
     *     {@link UMLActionSequence }
     *     
     */
    public UMLStateExit getStateExit() {
        return stateExit;
    }

    /**
     * 设置stateExit属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link UMLActionSequence }
     *     
     */
    public void setStateExit(UMLStateExit value) {
        this.stateExit = value;
    }
    
    /**
     * 获取stateEntry属性的值。
     * 
     * @return
     *     possible object is
     *     {@link UMLActionSequence }
     *     
     */
    public UMLStateEntry getStateEntry() {
        return stateEntry;
    }

    /**
     * 设置stateEntry属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link UMLActionSequence }
     *     
     */
    public void setStateEntry(UMLStateExit value) {
        this.stateExit = value;
    }

    /**
     * 获取stateInternalTransition属性的值。
     * 
     * @return
     *     possible object is
     *     {@link UMLStateInternalTransition }
     *     
     */
    public UMLStateInternalTransition getStateInternalTransition() {
        return stateInternalTransition;
    }

    /**
     * 设置stateInternalTransition属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link UMLStateInternalTransition }
     *     
     */
    public void setStateInternalTransition(UMLStateInternalTransition value) {
        this.stateInternalTransition = value;
    }

    /**
     * 获取name属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * 设置name属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * 获取namespace属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * 设置namespace属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNamespace(String value) {
        this.namespace = value;
    }

    /**
     * 获取visibility属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVisibility() {
        return visibility;
    }

    /**
     * 设置visibility属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVisibility(String value) {
        this.visibility = value;
    }

    /**
     * 获取xmiId属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getXmiId() {
        return xmiId;
    }

    /**
     * 设置xmiId属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setXmiId(String value) {
        this.xmiId = value;
    }

	@Override
	public UMLModelElementTaggedValue getUmlModelElementTaggedValue() {
		// TODO Auto-generated method stub
		return this.modelElementTaggedValue;
	}

}
