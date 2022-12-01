package com.util;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import com.bean.UML.*;
import com.bean.UML.XMI.XMIContent;

public class ReqUtil {

	public static UMLDiagram getDiagram(XMI xmi) {
		return Optional.ofNullable(xmi).map(XMI::getXMIContent).map(XMIContent::getDiagram)
				.map(diagrams -> diagrams.stream()
						.filter(diagram -> diagram.getDiagramType().equals(NameEnum.CustomDiagram.toString())).findFirst().get())
				.orElse(null);
	}
	
	public static List<UMLClassifierRole> getAllClassifierRole(XMI xmi){
		return XMIUtil.getTopPackage(xmi).getUmlNamespaceOwnedElement().getUmlCollaboration().getNamespaceOwnedElement().getClassifierRole();
	}
	
	public static List<UMLClassifierRole> filterCRByType(List<UMLClassifierRole> lists,String type){
		return lists.stream().map(umlClassifierRole -> {
			String name = Optional.ofNullable(umlClassifierRole)
					.map(UMLClassifierRole::getModelElementStereotype)
					.map(UMLModelElementStereotype::getStereotype).map(UMLStereotype::getName).orElse(null);
			if (name != null && name.equals(type)) {
				return umlClassifierRole;
			}
			return null;
		}).filter(role -> role != null).collect(Collectors.toList());
	}
	
	public static List<UMLClassifierRole> getAllRequirements(XMI xmi) {
		return filterCRByType(getAllClassifierRole(xmi),"requirement");
	}

	public static List<UMLTaggedValue> getAllTaggedValue(XMI xmi) {
		return xmi.getXMIContent().getTaggedValue();
	}

	/**
	 * 
	 * @param xmi
	 * @param id
	 * @return {"id":�����id,"text":�����text}
	 */
	public static HashMap<String, String> getReqContentByID(XMI xmi, String id) {
		HashMap<String, String> m = new HashMap<String, String>();
		List<UMLTaggedValue> taggedValues = getAllTaggedValue(xmi).stream()
				.filter(umlTaggedValue -> umlTaggedValue.getModelElement().equals(id)).collect(Collectors.toList());
		m.put("id", taggedValues.stream().filter(umlTaggedValue -> umlTaggedValue.getTag().equals("id")).findFirst()
				.get().getValue());
		m.put("text", taggedValues.stream().filter(umlTaggedValue -> umlTaggedValue.getTag().equals("text")).findFirst()
				.get().getValue());
		return m;
	}
	
	static List<UMLDependency> getAllDependency(XMI xmi){
		return XMIUtil.getTopPackage(xmi).getUmlNamespaceOwnedElement().getUmlDependency();
	}
	
	static List<UMLDependency> filterDependencyByType(List<UMLDependency> lists,String type){
		return lists.stream().filter(a -> {
			String name = Optional.ofNullable(a).map(UMLDependency::getModelElementTaggedValue)
					.map(taggedValue -> XMIUtil.getTaggedValueByTag("ea_type", taggedValue)).orElse(null);
			if (name != null && name.equals(type)) {
				return true;
			}
			return false;
		}).collect(Collectors.toList());
	}
	
	/**
	 * dempose����dependency�Ļ������޸�stereotypeΪdempose
	 * 
	 * @param xmi
	 * @return
	 */
	public static List<UMLDependency> getAllDempose(XMI xmi) {
		return filterDependencyByType(getAllDependency(xmi),"dempose");
	}

	/**
	 * dempose����dependency�Ļ������޸�stereotypeΪderiveReqt
	 * 
	 * @param xmi
	 * @return
	 */
	public static List<UMLDependency> getAllDerive(XMI xmi) {
		return filterDependencyByType(getAllDependency(xmi),"deriveReqt");
	}

	/**
	 * 
	 * @param xmi
	 * @param dependency
	 * @return {"source":�ߵ��������id,"end":�ߵ��յ�����id}
	 */
	public static HashMap<String, String> getPointsOfDependency(XMI xmi, UMLDependency dependency) {
		HashMap<String, String> m = new HashMap<String, String>();
		List<UMLClassifierRole> requirements = getAllRequirements(xmi);
		String value = requirements.stream()
				.filter(requirement -> requirement.getXmiId().equals(dependency.getClient())).findFirst().get()
				.getXmiId();
		m.put("source", getReqContentByID(xmi, value).get("id"));
		value = requirements.stream().filter(requirement -> requirement.getXmiId().equals(dependency.getSupplier()))
				.findFirst().get().getXmiId();
		m.put("end", getReqContentByID(xmi, value).get("id"));
		return m;
	}

	public static List<UMLClassifierRole> getRequirementsByOwnerID(XMI xmi, UMLDiagram owner) {
		// TODO Auto-generated method stub
		List<UMLClassifierRole> roles = getAllRequirements(xmi);
		return roles.stream().filter(role -> isOwnedByDiagram(role.getXmiId(), owner)).collect(Collectors.toList());
	}

	private static boolean isOwnedByDiagram(String roleID, UMLDiagram owner) {
		// TODO Auto-generated method stub

		return Optional.ofNullable(owner).map(UMLDiagram::getDiagramElement)
				.map(UMLDiagramElement::getDiagramElementSub).get().stream()
				.anyMatch(sub -> sub.getSubject().equals(roleID));
	}
	
}
