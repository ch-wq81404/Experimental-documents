package com.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.bean.UML.*;
import com.bean.UML.XMI.XMIContent;

public class ActUtil {

	public static UMLDiagram getDiagram(XMI xmi) {
		// TODO Auto-generated method stub
		return Optional.ofNullable(xmi).map(XMI::getXMIContent)
				.map(XMIContent::getDiagram).map(diagrams -> diagrams.stream()
						.filter(diagram -> diagram.getDiagramType().equals(NameEnum.ActivityDiagram.toString())).findFirst().get())
				.orElse(null);
	}

	static List<UMLActionState> getAllActionState(XMI xmi) {
		UMLPackage p = XMIUtil.getTopPackage(xmi);
		return Optional.ofNullable(p).map(UMLPackage::getUmlNamespaceOwnedElement)
				.map(UMLNamespaceOwnedElement::getActivityModel).map(UMLActivityModel::getStateMachineTop)
				.map(UMLStateMachineTop::getCompositeState).map(UMLCompositeState::getCompositeStateSubstate)
				.map(UMLCompositeStateSubstate::getUmlActionState).orElse(null);

	}
	
	public static UMLState filterById(List<UMLState> lists, String xmiid){
		return lists.stream().filter(action->action.getXmiId().equals(xmiid)).findFirst().orElse(null);
	}
	
	static List<UMLActionState> filterActionStateByType(List<UMLActionState> lists, String type) {
		return lists.stream().map(action -> {
			String s = XMIUtil.getTaggedValueByTag("ea_stype", action.getModelElementTaggedValue());
			if (s != null && s.equals(type)) {
				return action;
			}
			return null;
		}).filter(c -> c != null).collect(Collectors.toList());
	}

	static List<UMLActionState> getAllActPartition(XMI xmi) {
		return filterActionStateByType(getAllActionState(xmi), "ActivityPartition");
	}

	public static List<UMLActionState> getActPartitionByDiagram(XMI xmi, UMLDiagram diagram) {
		return getAllActPartition(xmi).stream().filter(part -> isOwnedByDiagram(part.getXmiId(), diagram))
				.collect(Collectors.toList());
	}

	public static List<UMLActionState> getActionByActPartition(XMI xmi, UMLActionState actPart) {
		return getAllAction(xmi).stream().filter(action -> XMIUtil
				.getTaggedValueByTag("owner", action.getModelElementTaggedValue()).equals(actPart.getXmiId()))
				.collect(Collectors.toList());
	}

	public static List<UMLActionState> getAllAction(XMI xmi) {
		return filterActionStateByType(getAllActionState(xmi), "Action");
	}

	private static boolean isOwnedByDiagram(String roleID, UMLDiagram owner) {
		// TODO Auto-generated method stub

		return Optional.ofNullable(owner).map(UMLDiagram::getDiagramElement)
				.map(UMLDiagramElement::getDiagramElementSub).get().stream()
				.anyMatch(sub -> sub.getSubject().equals(roleID));
	}

	public static List<UMLPseudoState> getAllPseudoState(XMI xmi) {
		UMLPackage p = XMIUtil.getTopPackage(xmi);
		return Optional.ofNullable(p).map(UMLPackage::getUmlNamespaceOwnedElement)
				.map(UMLNamespaceOwnedElement::getActivityModel).map(UMLActivityModel::getStateMachineTop)
				.map(UMLStateMachineTop::getCompositeState).map(UMLCompositeState::getCompositeStateSubstate)
				.map(UMLCompositeStateSubstate::getUmlPseudoState).orElse(null);
	}

	public static List<UMLTransition> getAllTransition(XMI xmi) {
		UMLPackage p = XMIUtil.getTopPackage(xmi);
		return Optional.ofNullable(p).map(UMLPackage::getUmlNamespaceOwnedElement)
				.map(UMLNamespaceOwnedElement::getActivityModel).map(UMLActivityModel::getStateMachineTransitions)
				.map(UMLStateMachineTransitions::getTransition).orElse(null);
	}

	public static List<UMLTransition> filterTransitionByTag(XMI xmi,String tag) {
		return getAllTransition(xmi).stream().map(action -> {
			String s = XMIUtil.getTaggedValueByTag("ea_type", action.getModelElementTaggedValue());
			if (s != null && s.equals(tag)) {
				return action;
			}
			return null;
		}).filter(c -> c != null).collect(Collectors.toList());
	}

//	public static List<UMLTransition> getAllControlFlow(XMI xmi) {
//		List<UMLTransition> transitions=getAllTransition(xmi);
//		return transitions.stream().map(action -> {
//			String s = XMIUtil.getTaggedValueByTag("ea_type", action.getModelElementTaggedValue());
//			if (s != null && s.equals("ControlFlow")) {
//				return action;
//			}
//			return null;
//		}).filter(c -> c != null).collect(Collectors.toList());
//	}

	/**
	 * 
	 * @param xmi
	 * @return keySet{source action, source Type, target action, target Type]
	 * 			如果端点不是action，source/target action字段是端点的xmiid
	 * 			如果是action，source/target action字段是活动名称
	 */
	public static HashMap<String, String> getPointsOfControlFlow(XMI xmi, UMLTransition trans) {
		HashMap<String, String> rlt = new HashMap<String, String>();
		String tag = XMIUtil.getTaggedValueByTag("ea_sourceType", trans.getModelElementTaggedValue());
		if (!tag.equals("Action"))
			rlt.put("sourceAction", trans.getSource());
		else
			rlt.put("sourceAction", XMIUtil.getTaggedValueByTag("ea_sourceName", trans.getModelElementTaggedValue()));
		rlt.put("sourceType", tag);

		tag = XMIUtil.getTaggedValueByTag("ea_targetType", trans.getModelElementTaggedValue());
		if (!tag.equals("Action"))
			rlt.put("targetAction", trans.getTarget());
		else
			rlt.put("targetAction", XMIUtil.getTaggedValueByTag("ea_targetName", trans.getModelElementTaggedValue()));
		rlt.put("targetType", tag);
		return rlt;
	}
	
	/**
	 * 
	 * @param xmi
	 * @return keySet{source: actionpin, target : actionpin]
	 */
	public static Map<String, UMLClassifierRole> getPointsOfObjectFlow(XMI xmi, UMLTransition trans, List<UMLState> lists) {
		Map<String, UMLClassifierRole> rlt = new HashMap<String, UMLClassifierRole>();
		rlt.put("source",(UMLClassifierRole) filterById(lists,trans.getSource()));
		rlt.put("target",(UMLClassifierRole) filterById(lists, trans.getTarget()));
		return rlt;
	}
	
	public static List<UMLState> getAllActionPin(XMI xmi){
		UMLPackage p = XMIUtil.getTopPackage(xmi);
		return Optional.ofNullable(p).map(UMLPackage::getUmlNamespaceOwnedElement)
				.map(UMLNamespaceOwnedElement::getUmlCollaboration).map(UMLCollaboration::getNamespaceOwnedElement)
				.map(UMLNamespaceOwnedElement::getClassifierRole).orElse(null).stream()
				.map(action -> {
					String s = XMIUtil.getTaggedValueByTag("ea_stype", action.getModelElementTaggedValue());
					if (s != null && s.equals("ActionPin")) {
						return action;
					}
					return null;
				}).filter(c -> c != null).collect(Collectors.toList());
	}
	
	/**
	 * 
	 * @param o
	 * @param valueTypes 
	 * @return {"name": ,"valueType": , "owner":(在某个活动节点上)}
	 */
	public static Map<String,String> getObjectInfo(UMLClassifierRole o, List<UMLState> valueTypes, XMI xmi){
		if(o!=null){
			Map<String,String> rlt=new HashMap<String,String>();
			rlt.put("name", o.getName());
			UMLState c=(UMLState)filterById(valueTypes, XMIUtil.getTaggedValueByTag("classifier", o.getModelElementTaggedValue()));
			if(c!=null)
				rlt.put("valueType", c.getName());
			
			c=filterById(getAllActionState(xmi).stream().map(a->(UMLState)a).collect(Collectors.toList()),
					XMIUtil.getTaggedValueByTag("owner", o.getModelElementTaggedValue()));
			if(c!=null)
				rlt.put("owner", c.getName());
			return rlt;
		}
		return null;
	}
	
}
