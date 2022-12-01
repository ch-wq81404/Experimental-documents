package com.mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.bean.UML.UMLActionState;
import com.bean.UML.UMLClassifierRole;
import com.bean.UML.UMLDiagram;
import com.bean.UML.UMLPseudoState;
import com.bean.UML.UMLState;
import com.bean.UML.UMLTransition;
import com.bean.UML.UMLTransitionGuard;
import com.bean.UML.XMI;
import com.bean.onto.OntoGenerator;
import com.logger.MyLogger;
import com.util.ActUtil;
import com.util.BDDUtil;
import com.util.TraceUtil;
import com.util.XMIUtil;

public class ActMapper {
	public static void map(XMI xmi, OntoGenerator ont) {
		// System.out.println("开始转换活动图。当前公理个数 ："+ont.getAximCount());
		System.out.println("Start transforming the activity diagram. Number of current axioms:" + ont.getAximCount());
		UMLDiagram diagram = ActUtil.getDiagram(xmi);
		if (diagram != null) {
			ont.createIndividualOfClass(diagram.getName(), "ActivityDiagram");
			PseudoStateMap(xmi, ont);
			ActPartitionMap(xmi, ont, diagram);
			EdgeMap(xmi, ont);
			/**
			 * 0.获得图中所有的活动分区，根据classifier属性与块建立allocate关系；done
			 * 1.对于每个活动结点，寻找是否有对应的块方法，若有，设置mapTo关系； done
			 * 2.对于图中每个ControlFlow，source和target设置为非重复活动节点，以及hasSource、hasTarget属性；done
			 * 3.根据结点找活动分区，设置边与活动分区的hasTransition属性； （这个用HERMIT推理器推理即可）
			 * 4.对于图中每个ObjectFlow，source和target设置为活动节点，对象设置为带有hasValueType属性的Object，以及hasSource、hasTarget、hasObject；done
			 * 5.对于每个活动结点，寻找是否有对应的块方法， 若有，检查Object的hasValueType属性值与块方法的对应值是否一致，
			 * done 若不，提示错误 else 设置mapTo关系； else warning；done
			 * 6.根据结点找活动分区，设置边与活动分区的hasTransition属性； skip
			 * 7.检查边是否只有一个hasTarget,一个hasSource done
			 * 8.检查ControlFlow是否有guard，有则添加 done
			 */

		}
		// System.out.println("转换完成。当前公理个数 ："+ont.getAximCount());
		System.out.println("Transform over. Number of current axioms:" + ont.getAximCount());
	}

	private static void EdgeMap(XMI xmi, OntoGenerator ont) {
		// TODO Auto-generated method stub
		controlFlowMap(xmi, ont);
		objectFlowMap(xmi, ont);
		ont.infer();
	}

	private static void objectFlowMap(XMI xmi, OntoGenerator ont) {
		// TODO Auto-generated method stub
		List<UMLTransition> objectFlows = ActUtil.filterTransitionByTag(xmi, "ObjectFlow");
		List<UMLState> pins = ActUtil.getAllActionPin(xmi);
		List<UMLState> valueTypes = BDDUtil.getAllValueTypes(xmi);
		int count = 0;
		for (UMLTransition flow : objectFlows) {
			Map<String, UMLClassifierRole> points = ActUtil.getPointsOfObjectFlow(xmi, flow, pins);
			if (points.getOrDefault("source", null) == null || points.getOrDefault("target", null) == null) {
				MyLogger.LOGGER.warn("There are object flow without complete information in ActivityDiagram");
				continue;
			}
			String name = "ObjectFlow" + count;
			count++;
			ont.createIndividualOfClass(name, "ObjectFlow");
			UMLTransitionGuard g = Optional.ofNullable(flow).map(UMLTransition::getTransitionGuard).orElse(null);
			if (g != null) {
				ont.createIndividualOfClass(name + "Guard", "Guard");
				ont.createDataPropertyAxiom("hasGuardValue", name + "Guard",
						g.getGuard().getGuardExpression().getBooleanExpression().getBody());
				ont.createObjectPropertyAxiom("hasGuard", name, name + "Guard");
			}
			for (String a : new String[] { "Source", "Target" }) {
				Map<String, String> pointInfo = ActUtil.getObjectInfo(points.get(a.toLowerCase()), valueTypes, xmi);
				ont.createIndividualOfClass(pointInfo.getOrDefault("name", null), "Object");
				ont.createObjectPropertyAxiom("hasObject", name, pointInfo.get("name"));
				ont.createDataPropertyAxiom("hasValueType", pointInfo.getOrDefault("name", null),
						pointInfo.getOrDefault("valueType", null));
				ont.createObjectPropertyAxiom("has" + a, name, pointInfo.getOrDefault("owner", null));
			}
		}
	}

	private static void controlFlowMap(XMI xmi, OntoGenerator ont) {
		// TODO Auto-generated method stub
		List<UMLTransition> controlFlows = ActUtil.filterTransitionByTag(xmi, "ControlFlow");
		int count = 0;
		for (UMLTransition flow : controlFlows) {
			String name = "ControlFlow" + count;
			ont.createIndividualOfClass(name, "ObjectFlow");
			UMLTransitionGuard g = Optional.ofNullable(flow).map(UMLTransition::getTransitionGuard).orElse(null);
			if (g != null) {
				ont.createIndividualOfClass(name + "Guard", "Guard");
				ont.createDataPropertyAxiom("hasGuardValue", name + "Guard",
						g.getGuard().getGuardExpression().getBooleanExpression().getBody());
				ont.createObjectPropertyAxiom("hasGuard", name, name + "Guard");
			}
			Map<String, String> points = ActUtil.getPointsOfControlFlow(xmi, flow);
			if (!points.get("sourceType").equals("Action")) {
				TraceUtil.addSourceOfTransition(points.get("sourceAction"), name, ont);
			} else {
				ont.createObjectPropertyAxiom("hasSource", name, points.get("sourceAction"));
			}
			if (!points.get("targetType").equals("Action")) {
				TraceUtil.addTargetOfTransition(points.get("targetAction"), name, ont);
			} else {
				ont.createObjectPropertyAxiom("hasTarget", name, points.get("targetAction"));
			}
			count++;
		}

	}

	private static void PseudoStateMap(XMI xmi, OntoGenerator ont) {
		// TODO Auto-generated method stub
		List<UMLPseudoState> specialNodes = ActUtil.getAllPseudoState(xmi);
		int beginCount = 0, endCount = 0, mergeCount = 0, splitCount = 0;
		for (UMLPseudoState specialNode : specialNodes) {
			String name = specialNode.getName();
			switch (XMIUtil.getTaggedValueByTag("ea_ntype", specialNode.getModelElementTaggedValue())) {
			case "3": {
				ont.createIndividualOfClass(name + beginCount, "Begin");
				ont.createDataPropertyAxiom("hasXmiID", name + beginCount, specialNode.getXmiId());
				beginCount++;
				break;
			}
			case "4": {
				ont.createIndividualOfClass(name + endCount, "End");
				ont.createDataPropertyAxiom("hasXmiID", name + endCount, specialNode.getXmiId());
				endCount++;
				break;
			}
			case "102": {
				ont.createIndividualOfClass(name + endCount, "End");
				ont.createDataPropertyAxiom("hasXmiID", name + endCount, specialNode.getXmiId());
				endCount++;
				break;
			}
			case "101": {
				ont.createIndividualOfClass(name + endCount, "End");
				ont.createDataPropertyAxiom("hasXmiID", name + endCount, specialNode.getXmiId());
				endCount++;
				break;
			}
			case "100": {
				ont.createIndividualOfClass(name + beginCount, "Begin");
				ont.createDataPropertyAxiom("hasXmiID", name + beginCount, specialNode.getXmiId());
				beginCount++;
				break;
			}
			case "0": {
				ont.createIndividualOfClass("Merge" + mergeCount, "MergeNode");
				ont.createDataPropertyAxiom("hasXmiID", "Merge" + mergeCount, specialNode.getXmiId());
				mergeCount++;
				break;
			}
			case "11": {
				ont.createIndividualOfClass(name + splitCount, "SplitNode");
				ont.createDataPropertyAxiom("hasXmiID", name + splitCount, specialNode.getXmiId());
				splitCount++;
				break;
			}
			}
		}
	}

	private static void ActionMap(XMI xmi, OntoGenerator ont, UMLActionState actPart) {
		// TODO Auto-generated method stub
		List<UMLActionState> actions = ActUtil.getActionByActPartition(xmi, actPart);
		List<String> names = new ArrayList<String>();
		for (UMLActionState action : actions) {
			String tagValue = XMIUtil.getTaggedValueByTag("$ea_xref_property", action.getModelElementTaggedValue());
			if (tagValue == null) {
				ont.createIndividualOfClass(action.getName(), "Action");
				ont.createObjectPropertyAxiom("hasAction", actPart.getName(), action.getName());
			} else {
				if (tagValue.contains("AcceptEvent"))
					ont.createIndividualOfClass(action.getName(), "InEvent");
				else if (tagValue.contains("SendSignal"))
					ont.createIndividualOfClass(action.getName(), "OutEvent");
				ont.createObjectPropertyAxiom("hasEvent", actPart.getName(), action.getName());
			}
			ont.createDataPropertyAxiom("hasXmiID", action.getName(), action.getXmiId());
			names.add(action.getName());
		}
		TraceUtil.traceActionAndFunction(names, ont);
	}

	private static void ActPartitionMap(XMI xmi, OntoGenerator ont, UMLDiagram diagram) {
		// TODO Auto-generated method stub
		List<UMLActionState> actParts = ActUtil.getActPartitionByDiagram(xmi, diagram);
		Map<String, String> map = new HashMap<String, String>();
		for (UMLActionState actPart : actParts) {
			ont.createIndividualOfClass(actPart.getName(), "ActPartition");
			ont.createObjectPropertyAxiom("haveActPartition", diagram.getName(), actPart.getName());
			ont.createDataPropertyAxiom("hasXmiID", actPart.getName(), actPart.getXmiId());
			String classname = XMIUtil.getTaggedValueByTag("classname", actPart.getModelElementTaggedValue());
			map.put(actPart.getName(), classname);
			ActionMap(xmi, ont, actPart);
		}
		TraceUtil.traceBlock2ActPart(map, ont);
	}

}
