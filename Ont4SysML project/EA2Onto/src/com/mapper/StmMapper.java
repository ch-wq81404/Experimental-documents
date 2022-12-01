package com.mapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.bean.UML.UMLActionSequence;
import com.bean.UML.UMLActionSequenceAction;
import com.bean.UML.UMLEvent;
import com.bean.UML.UMLSimpleState;
import com.bean.UML.UMLState;
import com.bean.UML.UMLStateEntry;
import com.bean.UML.UMLStateExit;
import com.bean.UML.UMLStateInternalTransition;
import com.bean.UML.UMLStateMachine;
import com.bean.UML.UMLTransition;
import com.bean.UML.UMLTransitionEffect;
import com.bean.UML.UMLTransitionGuard;
import com.bean.UML.UMLTransitionTrigger;
import com.bean.UML.UninterpretedAction;
import com.bean.UML.XMI;
import com.bean.onto.OntoGenerator;
import com.util.ActUtil;
import com.util.BDDUtil;
import com.util.StmUtil;
import com.util.TraceUtil;
import com.util.XMIUtil;

/**
 * 0.找到所有状态机图，新建个体和hasXmiId、possess属性 1.根据图，找区域和非区域状态，新建个体，hasRegion,hasState属性
 * 2.对于每个区域或状态，找do,exit,entry，新建对应属性 3.找边 4.trace
 * ①根据do、trigger、guard等，找活动节点，建立function与transition的map关系
 */

public class StmMapper {
	public static void map(XMI xmi, OntoGenerator ont) {
		// System.out.println("开始转换状态机图。当前公理个数 ："+ont.getAximCount());
		System.out.println("Start transforming the state machine. Number of current axioms:" + ont.getAximCount());
		List<UMLStateMachine> diagrams = StmUtil.getDiagrams(xmi);
		List<UMLState> blocks = BDDUtil.getAllBlocks(xmi).stream().map(b -> (UMLState) b).collect(Collectors.toList());
		List<UMLState> states = StmUtil.getAllStates(xmi);
		for (UMLStateMachine diagram : diagrams) {
			ont.createIndividualOfClass(diagram.getName(), "StateMachineDiagram");
			ont.createDataPropertyAxiom("hasXmiID", diagram.getName(), diagram.getXmiId());
			ont.createObjectPropertyAxiom("possess",
					ActUtil.filterById(blocks,
							XMIUtil.getTaggedValueByTag("owner", diagram.getModelElementTaggedValue())).getName(),
					diagram.getName());
			for (UMLState state : states) {
				if (XMIUtil.getTaggedValueByTag("owner", state.getUmlModelElementTaggedValue())
						.equals(diagram.getXmiId())) {
					ont.createIndividualOfClass(safeName(state.getName()), "State");
					ont.createObjectPropertyAxiom("hasState", diagram.getName(), safeName(state.getName()));
					proInStateMap(xmi, (UMLSimpleState) state, ont);
				}
			}
		}
		TransitionMap(xmi, ont);
		// System.out.println("转换完成。当前公理个数 ："+ont.getAximCount());
		System.out.println("Transform over. Number of current axioms:" + ont.getAximCount());
	}

	private static void TransitionMap(XMI xmi, OntoGenerator ont) {
		// TODO Auto-generated method stub
		List<UMLTransition> transitions = StmUtil.getAllTransitions(xmi);
		int count = 0;
		for (UMLTransition t : transitions) {
			String name = "Transition" + count;
			count++;
			ont.createIndividualOfClass(name, "Transition");
			proOnTransitionMap(xmi, t, name, ont);
			Map<String, String> points = StmUtil.getPointsOfTransition(xmi, t);
			if (!points.get("sourceType").equals("State")) {
				TraceUtil.addSourceOfTransition(points.get("sourceState"), name, ont);
			} else {
				ont.createObjectPropertyAxiom("hasSource", name, points.get("sourceState"));
			}
			if (!points.get("targetType").equals("State")) {
				TraceUtil.addTargetOfTransition(points.get("targetState"), name, ont);
			} else {
				ont.createObjectPropertyAxiom("hasTarget", name, points.get("targetState"));
			}
		}
		ont.infer();
	}

	private static void proOnTransitionMap(XMI xmi, UMLTransition t, String name, OntoGenerator ont) {
		// TODO Auto-generated method stub
		UMLTransitionGuard g = Optional.ofNullable(t).map(UMLTransition::getTransitionGuard).orElse(null);
		if (g != null) {
			ont.createIndividualOfClass(name + "Guard", "Guard");
			ont.createDataPropertyAxiom("hasGuardValue", name + "Guard",
					g.getGuard().getGuardExpression().getBooleanExpression().getBody());
			ont.createObjectPropertyAxiom("hasGuard", name, name+"Guard");
		}

		if (t.getTransitionEffect() != null) {
			String value = Optional.ofNullable(t).map(UMLTransition::getTransitionEffect)
					.map(UMLTransitionEffect::getUMLActionSequence)
					.map(UMLActionSequence::getActionSequenceAction)
					.map(UMLActionSequenceAction::getUninterpretedAction).map(UninterpretedAction::getName)
					.orElse(null);
			ont.createIndividualOfClass(name + "Effect", "Effect");
			ont.createDataPropertyAxiom("hasEffectValue", name + "Effect", value);
			ont.createObjectPropertyAxiom("hasEffect", name, name + "Effect");
		}

		if (t.getTransitionTrigger() != null) {
			String value = Optional.ofNullable(t).map(UMLTransition::getTransitionTrigger)
					.map(UMLTransitionTrigger::getEvent).map(UMLEvent::getName).orElse(null);
			ont.createIndividualOfClass(name + "Trigger", "Trigger");
			ont.createDataPropertyAxiom("hasTriggerValue", name + "Trigger", value);
			ont.createObjectPropertyAxiom("hasTrigger", name, name + "Trigger");
		}
	}

	private static void proInStateMap(XMI xmi, UMLSimpleState state, OntoGenerator ont) {
		// TODO Auto-generated method stub
		if (state.getStateEntry() != null) {
			String name = "Entry in " + state.getName();name=safeName(name);
			String value = Optional.ofNullable(state).map(UMLSimpleState::getStateEntry)
					.map(UMLStateEntry::getUMLActionSequence)
					.map(UMLActionSequence::getActionSequenceAction)
					.map(UMLActionSequenceAction::getUninterpretedAction).map(UninterpretedAction::getName)
					.orElse(null);
			ont.createIndividualOfClass(name, "Entry");
			ont.createDataPropertyAxiom("hasEntryValue", name, value);
			// MyLogger.LOGGER.warn("Please refine
			// 'relateToBlock','relateToPara' of "+name+".");
			ont.createObjectPropertyAxiom("hasEntry", safeName(state.getName()), name);
		}
		if (state.getStateInternalTransition() != null) {
			String name = "Do in " + state.getName();
			name=safeName(name);
			String value = Optional.ofNullable(state).map(UMLSimpleState::getStateInternalTransition)
					.map(UMLStateInternalTransition::getTransition).map(UMLTransition::getTransitionEffect)
					.map(UMLTransitionEffect::getUMLActionSequence).map(UMLActionSequence::getActionSequenceAction)
					.map(UMLActionSequenceAction::getUninterpretedAction).map(UninterpretedAction::getName)
					.orElse(null);
			ont.createIndividualOfClass(name, "Do");
			ont.createDataPropertyAxiom("hasDoValue", name, value);
			// MyLogger.LOGGER.warn("Please refine
			// 'relateToBlock','relateToPara' of "+name+".");
			ont.createObjectPropertyAxiom("hasDo", safeName(state.getName()), name);
		}
		if (state.getStateExit() != null) {
			String name = "Exit in " + state.getName();
			name=safeName(name);
			String value = Optional.ofNullable(state).map(UMLSimpleState::getStateExit)
					.map(UMLStateExit::getUMLActionSequence)
					.map(UMLActionSequence::getActionSequenceAction)
					.map(UMLActionSequenceAction::getUninterpretedAction).map(UninterpretedAction::getName)
					.orElse(null);
			ont.createIndividualOfClass(name, "Exit");
			ont.createDataPropertyAxiom("hasExitValue", name, value);
			// MyLogger.LOGGER.warn("Please refine
			// 'relateToBlock','relateToPara' of "+name+".");
			ont.createObjectPropertyAxiom("hasExit", safeName(state.getName()), name);
		}
	}
	
	public static String safeName(String name){
		return name.replace(" ", "_");
	}
}
