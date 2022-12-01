package com.util;

import java.util.Optional;

import com.bean.UML.UMLDiagram;
import com.bean.UML.UMLModel;
import com.bean.UML.UMLModelElementTaggedValue;
import com.bean.UML.UMLNamespaceOwnedElement;
import com.bean.UML.UMLPackage;
import com.bean.UML.UMLTaggedValue;
import com.bean.UML.XMI;
import com.bean.UML.XMI.XMIContent;

/**
 * XMI�У�pkg����֯��ʽ��
 * 
 * ��pkg 
 * 		overall pkg 
 * 		signal pkg 
 * 		valueType pkg
 */
public class XMIUtil {

	/**
	 * TopPackageָ���ǰ�������Ԫ�ص�package������package�µĵ�һ��package
	 *
	 * @param xmi
	 * @return overall pkg.map(UMLNamespaceOwnedElement::getUmlCollaboration)
				.map(UMLCollaboration::getNamespaceOwnedElement)
	 */
	public static UMLPackage getTopPackage(XMI xmi) {
		return Optional.ofNullable(xmi).map(XMI::getXMIContent).map(XMIContent::getModel)
				.map(UMLModel::getNamespaceOwnedElement).map(UMLNamespaceOwnedElement::getUmlPackage).get().stream()
				.findFirst().map(UMLPackage::getUmlNamespaceOwnedElement).map(UMLNamespaceOwnedElement::getUmlPackage)
				.get().stream().findFirst().get();
		/**
		 * �൱�������еķ���ֵ����Ϊnullʱ������д������з���ֵΪnull�����׳��쳣 return xmi .getXMIContent()
		 * .getModel() .getNamespaceOwnedElement() .getUmlPackage().get()
		 * .stream().findFirst().get(); Ϊ��ʡȥ���ifǶ���жϵĴ��룬ʹ��Optional��
		 */
	}

	/**
	 * ���չؼ����ҵ�pkg
	 *
	 * @param xmi
	 * @return signal pkg or valueType
	 */
	public static UMLPackage getPackage(XMI xmi, NameEnum NameEnum) {
		UMLPackage topPackage = Optional.ofNullable(xmi).map(XMI::getXMIContent).map(XMIContent::getModel)
				.map(UMLModel::getNamespaceOwnedElement).map(UMLNamespaceOwnedElement::getUmlPackage).get().stream()
				.findFirst().get();
		return Optional.ofNullable(topPackage).map(UMLPackage::getUmlNamespaceOwnedElement)
				.map(UMLNamespaceOwnedElement::getUmlPackage)
				.map(umlPackages -> umlPackages.stream()
						.filter(umlPackage -> umlPackage.getName().contains(NameEnum.name())).findAny().get())
				.get();
	}

	public static UMLDiagram getDiagramByID(String diagramId, XMI xmi) {
		return Optional.ofNullable(xmi).map(XMI::getXMIContent).map(XMIContent::getDiagram).get().stream()
				.filter(umlDiagram -> umlDiagram.getXmiId().equals(diagramId)).findFirst().orElse(null);
	}

	public static String getTaggedValueByTag(String tag, UMLModelElementTaggedValue umlModelElementTaggedValue) {
		return umlModelElementTaggedValue.getTaggedValue().stream()
				.filter(umlTaggedValue -> umlTaggedValue.getTag().equals(tag)).findFirst().map(UMLTaggedValue::getValue)
				.orElse(null);
	}
	
	public static String safeName(String name){
		return name.replace(" ", "_");
	}
	
	
}
