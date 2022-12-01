package com.util;

import com.bean.UML.*;
import com.bean.UML.XMI.XMIContent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BDDUtil {

	public static UMLDiagram getDiagram(XMI xmi) {
		return Optional.ofNullable(xmi).map(XMI::getXMIContent)
				.map(XMIContent::getDiagram).map(diagrams -> diagrams.stream()
						.filter(diagram -> diagram.getDiagramType().equals(NameEnum.ClassDiagram.toString())).findFirst().get())
				.orElse(null);
	}

	public static List<UMLClass> getAllBlocks(XMI xmi) {
		UMLPackage p = XMIUtil.getTopPackage(xmi);
		return Optional.ofNullable(p).map(UMLPackage::getUmlNamespaceOwnedElement)
				.map(UMLNamespaceOwnedElement::getUmlClasses).get().stream().map(umlClass -> {
					String s = Optional.ofNullable(umlClass).map(UMLClass::getUmlModelElementStereotype)
							.map(UMLModelElementStereotype::getStereotype).map(UMLStereotype::getName).orElse(null);
					if (s != null && s.equals("block")) {
						return umlClass;
					}
					return null;
				}).filter(c -> c != null).collect(Collectors.toList());
	}

	public static List<UMLClass> getBlocksOfDiagram(XMI xmi, UMLDiagram diagram) {
		return getAllBlocks(xmi).stream().filter(b -> isOwnedByDiagram(b.getXmiId(), diagram))
				.collect(Collectors.toList());
	}

	private static boolean isOwnedByDiagram(String roleID, UMLDiagram owner) {
		// TODO Auto-generated method stub

		return Optional.ofNullable(owner).map(UMLDiagram::getDiagramElement)
				.map(UMLDiagramElement::getDiagramElementSub).get().stream()
				.anyMatch(sub -> sub.getSubject().equals(roleID));
	}

	/**
	 * library: 用户引入的类型库 cs：用户自定义的类型
	 * 
	 * @param xmi
	 * @return
	 */
	public static List<String> getValueTypes(XMI xmi) {
		List<String> rlt = new ArrayList<String>();
		rlt.add("void");
		rlt.addAll(getAllValueTypes(xmi).stream().map(c->c.getName()).collect(Collectors.toList()));
		return rlt;
	}

	public static List<UMLState> getAllValueTypes(XMI xmi){
		List<UMLState> rlt = new ArrayList<UMLState>();
		UMLPackage p = XMIUtil.getPackage(xmi, NameEnum.valueType);
		UMLPackage library = Optional.ofNullable(p).map(UMLPackage::getUmlNamespaceOwnedElement)
				.map(UMLNamespaceOwnedElement::getUmlPackage).get().stream().findFirst().orElse(null);
		if (library != null) {
			rlt.addAll(library.getUmlNamespaceOwnedElement().getUmlClasses());
		}
		List<UMLClass> cs = Optional.ofNullable(p).map(UMLPackage::getUmlNamespaceOwnedElement)
				.map(UMLNamespaceOwnedElement::getUmlClasses).orElse(null);
		if (cs != null)
			rlt.addAll(cs);
		return rlt;
	}
	
	public static List<UMLOperation> getOperationsOfBlock(XMI xmi, String c) {
		UMLClass umlClass = getAllBlocks(xmi).stream().filter(b -> b.getName().equals(c)).findFirst().get();
		return Optional.ofNullable(umlClass).map(UMLClass::getUmlClassifierFeature)
				.map(UMLClassifierFeature::getOperation).orElse(null);
	}

	public static List<UMLAttribute> getAttributesOfBlock(XMI xmi, String c) {
		UMLClass umlClass = getAllBlocks(xmi).stream().filter(b -> b.getName().equals(c)).findFirst().get();
		return Optional.ofNullable(umlClass).map(UMLClass::getUmlClassifierFeature)
				.map(UMLClassifierFeature::getAttribute).orElse(null);
	}

	public static List<UMLGeneralization> getAllGeneralizations(XMI xmi) {
		UMLPackage p = XMIUtil.getTopPackage(xmi);
		return Optional.ofNullable(p).map(UMLPackage::getUmlNamespaceOwnedElement)
				.map(UMLNamespaceOwnedElement::getUmlGeneralizations).orElse(null);
	}
	
	private static List<UMLAssociation> getAllAssociation(XMI xmi){
		return XMIUtil.getTopPackage(xmi).getUmlNamespaceOwnedElement().getUmlAssociations();
	}
	
	private static List<UMLAssociation> filteAssociationByType(List<UMLAssociation> lists,String type){
		return lists.stream().map(a -> {
			String name = Optional.ofNullable(a).map(UMLAssociation::getModelElementTaggedValue)
					.map(taggedValue -> XMIUtil.getTaggedValueByTag("ea_type", taggedValue)).orElse(null);
			if (name != null && name.equals(type)) {
				return a;
			}
			return null;
		}).filter(a -> a != null).collect(Collectors.toList());
	}

	public static List<UMLAssociation> getAllConnectors(XMI xmi) {
		// TODO Auto-generated method stub
		return filteAssociationByType(getAllAssociation(xmi),"Connector");
	}
	
	public static List<UMLAssociation> getAllAggregations(XMI xmi){
		return filteAssociationByType(getAllAssociation(xmi),"Aggregation");
	}
	
	/**
	 * 
	 * @param xmi
	 * @param aggregation
	 * @return keySet{sourceI, SourceT, targetI, TargetT]
	 */
	public static HashMap<String, String> getInstanceFromAssociation(XMI xmi,
			UMLModelElementTaggedValue umlModelElementTaggedValue) {
		HashMap<String, String> rlt = new HashMap<String, String>();
		String tag = XMIUtil.getTaggedValueByTag("lt", umlModelElementTaggedValue);
		if (tag != null)
			rlt.put("sourceI", tag.substring(1));
		else
			rlt.put("sourceI", null);
		rlt.put("SourceT", XMIUtil.getTaggedValueByTag("ea_sourceName", umlModelElementTaggedValue));
		
		tag = XMIUtil.getTaggedValueByTag("rt", umlModelElementTaggedValue);
		if (tag != null)
			rlt.put("targetI", tag.substring(1));
		else
			rlt.put("targetI", null);
		rlt.put("TargetT", XMIUtil.getTaggedValueByTag("ea_targetName", umlModelElementTaggedValue));
		return rlt;
	}

	public static List<UMLAssociation> getAssociationsOfDiagram(UMLDiagram diagram, List<UMLAssociation> list) {
		// TODO Auto-generated method stub
		return list.stream().filter(a -> isOwnedByDiagram(a.getXmiId(), diagram)).collect(Collectors.toList());
	}

	public static List<UMLGeneralization> getGeneralizationsOfDiagram(UMLDiagram diagram,
			List<UMLGeneralization> list) {
		// TODO Auto-generated method stub
		return list.stream().filter(a -> isOwnedByDiagram(a.getXmiId(), diagram)).collect(Collectors.toList());
	}

	

}
