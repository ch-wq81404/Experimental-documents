package com.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.bean.UML.UMLActivityModel;
import com.bean.UML.UMLCompositeState;
import com.bean.UML.UMLCompositeStateSubstate;
import com.bean.UML.UMLModelElementTaggedValue;
import com.bean.UML.UMLNamespaceOwnedElement;
import com.bean.UML.UMLPackage;
import com.bean.UML.UMLSimpleState;
import com.bean.UML.UMLState;
import com.bean.UML.UMLStateMachine;
import com.bean.UML.UMLStateMachineTop;
import com.bean.UML.UMLTaggedValue;
import com.bean.UML.UMLTransition;
import com.bean.UML.XMI;

public class StmUtil {

	public static List<UMLStateMachine> getDiagrams(XMI xmi) {
		// TODO Auto-generated method stub
		UMLPackage p = XMIUtil.getTopPackage(xmi);
		return Optional.ofNullable(p).map(UMLPackage::getUmlNamespaceOwnedElement)
				.map(UMLNamespaceOwnedElement::getStateMachine).orElse(null);
	}

	public static List<UMLTransition> getAllTransitions(XMI xmi) {
		return ActUtil.filterTransitionByTag(xmi, "StateFlow");
	}

	public static List<UMLState> getAllStates(XMI xmi) {
		UMLCompositeStateSubstate allStatesObj = getAllStatesObj(xmi);
		List<UMLSimpleState> umlSimpleState = allStatesObj.getUmlSimpleState();
		List<UMLState> list = new ArrayList<UMLState>();
		list.addAll(umlSimpleState);
		return list;
	}

	public static UMLCompositeStateSubstate getAllStatesObj(XMI xmi) {
		UMLPackage smPackage = XMIUtil.getTopPackage(xmi);
		return Optional.ofNullable(smPackage).map(UMLPackage::getUmlNamespaceOwnedElement)
				.map(UMLNamespaceOwnedElement::getActivityModel).map(UMLActivityModel::getStateMachineTop)
				.map(UMLStateMachineTop::getCompositeState).map(UMLCompositeState::getCompositeStateSubstate).get();
	}

	public static List<UMLState> getAllStateByRegionId(String regionId, XMI xmi) {
		return getAllStates(xmi).stream().filter(
				state -> XMIUtil.getTaggedValueByTag("$ea_xref_property", state.getUmlModelElementTaggedValue()) != null
						&& XMIUtil.getTaggedValueByTag("$ea_xref_property", state.getUmlModelElementTaggedValue())
								.contains(regionId)
						&& !XMIUtil.getTaggedValueByTag("$ea_xref_property", state.getUmlModelElementTaggedValue())
								.contains("GUID"))
				.collect(Collectors.toList());
	}

	public static List<String> getRegionIdBySystemName(String systemName, XMI xmi) {
		List<String> regions = new ArrayList<>();
		List<UMLTaggedValue> taggedValues = getAllStatesObj(xmi).getUmlSimpleState().stream()
				.map(UMLSimpleState::getModelElementTaggedValue).map(UMLModelElementTaggedValue::getTaggedValue)
				.map(umlTaggedValues -> umlTaggedValues.stream()
						.filter(umlTaggedValue -> umlTaggedValue.getTag().equals("$ea_xref_property")
								&& umlTaggedValue.getValue().contains(systemName))
						.findFirst().orElse(null))
				.collect(Collectors.toList());
		for (UMLTaggedValue umlTaggedValue : taggedValues) {
			if (umlTaggedValue == null)
				continue;
			String[] split = umlTaggedValue.getValue().split(";");
			boolean findSystem = false;
			for (int i = 0; i < split.length; i++) {
				if (split[i].contains(systemName)) {
					findSystem = true;
				}
				if (split[i].contains("GUID") && findSystem) {
					String regionId = split[i].substring(6, split[i].length() - 1);
					regions.add(regionId);
					break;
				}
			}

		}
		return regions;
	}

	public static UMLState getStateById(String stateId, XMI xmi) {
		List<UMLState> allStates = getAllStates(xmi);
		return allStates.stream().filter(umlState -> umlState.getXmiId().equals(stateId)).findFirst().orElse(null);
	}

	public static Map<String, String> getPointsOfTransition(XMI xmi, UMLTransition trans) {
		// TODO Auto-generated method stub
		HashMap<String, String> rlt = new HashMap<String, String>();
		String tag = XMIUtil.getTaggedValueByTag("ea_sourceType", trans.getModelElementTaggedValue());
		if (!tag.equals("State"))
			rlt.put("sourceState", trans.getSource());
		else
			rlt.put("sourceState", safeName(XMIUtil.getTaggedValueByTag("ea_sourceName", trans.getModelElementTaggedValue())));
		rlt.put("sourceType", tag);

		tag = XMIUtil.getTaggedValueByTag("ea_targetType", trans.getModelElementTaggedValue());
		if (!tag.equals("State"))
			rlt.put("targetState", trans.getTarget());
		else
			rlt.put("targetState", safeName(XMIUtil.getTaggedValueByTag("ea_targetName", trans.getModelElementTaggedValue())));
		rlt.put("targetType", tag);
		return rlt;
	}
	
	private static String safeName(String name){
		return name.replace(" ", "_");
	}
}
