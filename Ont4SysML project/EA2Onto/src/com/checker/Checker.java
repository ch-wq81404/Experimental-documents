package com.checker;

import java.util.Collections;
import java.util.List;

import com.bean.UML.XMI;
import com.bean.onto.OntoGenerator;
import com.logger.MyLogger;

public class Checker {
	/**
	 * BDD
	 * - 1.每个Block块都有individual，如果没有，提示BlockInstanceMissing，将影响可追溯性传递，可在该Block相关的边上定义 done
	 * - 检查方法：for xmi中BDD块，{for 从该块出发或到达该块的边，{if有实例名，break} 非break到达 return error}                  
	 * 2.aggregation边尽量使用1：1的multiplicity, 然后标明实例名。如果发现1：多（1：*除外），提示InappropriateMultiplicity from 某块 to 某块，1:1 with instance name is better
	 * 检查方法：BDD的边上是否有1：多（1：*除外）的multiplicity
	 * 3.aggregation边必须有实例名称
	 */

	/**
	 * Act
	 * 1.对象流上的object与块操作返回值应一致，如果没有，提示InconsistentValueType，可修改块操作的返回值类型或object类型
	 * 检查思路:ObjectFlow(f)^hasObject(f1,o)^hasValueType(o,v1)^hasSource(f1,a)^mapToBF(a,bf)^hasValueType(bf,v2)^equals(v1,v2) ->haveNoError(actPartition,"true")
	 */
	
	/**
	 * TIM
	 * 
	 */
	
	/**
	 * BDD.1,BDD.2, BDD.3
	 * @param xmi
	 */
	public static void preCheck(XMI xmi){
		
	}
	/**
	 * BDD.1, Act.1, TIM
	 * @param xmi
	 * @param ont
	 */
	public static void postCheck(XMI xmi, OntoGenerator ont){
		
	}
	/**
	 * inCheck
	 * BDD.1
	 */
	public static void BDDFirstCheck(XMI xmi, OntoGenerator ont) {
		// TODO Auto-generated method stub
		String prefix = ont.getShortPrefix();
		String rule1=String.format("tbox:sca(?c,%s:Block)->sqwrl:select(?c)", prefix);
		String rule2=String.format("tbox:sca(?c,%s:Block)^ abox:caa(?c,?i)->sqwrl:select(?c)", prefix);
		List<String> rlt1 = ont.queryBySQWRL(rule1, "c");
		List<String> rlt2=ont.queryBySQWRL(rule2, "c");
		Collections.sort(rlt1);
		Collections.sort(rlt2);
		for(int i=0,j=0;i<rlt1.size();i++){
			if(rlt1.get(i).equals(rlt2.get(j)))
				j++;
			else
				MyLogger.LOGGER.warn("Block '"+rlt1.get(i)+"' have no individuals. But it better have.");
		}
	}
	/**
	 * Act.1
	 * ObjectFlow(f)^hasObject(f1,o)^hasValueType(o,v1)^hasSource(f1,a)^mapToBF(a,bf)^hasValueType(bf,v2)^equals(v1,v2) ->haveNoError(actPartition,"true")
	 * @param xmi
	 * @param ont
	 */
	public static void ActObjectCheck(XMI xmi, OntoGenerator ont){
		String prefix = ont.getShortPrefix();
		String rule="%s:ObjectFlow(?f)^%s:hasObject(?f,?o)^%s:hasValueType(?o,?v1)^%s:hasSource(?f,?action)^%s:mapToBF(?action,?bf)"
				+ "%s:hasValueType(?bf,?v2)->sqwrl:select(?v1,?v2,?action)";
		rule=String.format(rule, prefix,prefix,prefix,prefix,prefix,prefix);
		List<String[]> rlt=ont.queryBySQWRL(rule, new String[]{"v1","v2","action"});
		for(int i=0;i<rlt.size();i++){
			if(!rlt.get(i)[0].equals(rlt.get(i)[1])){
				MyLogger.LOGGER.error("The type of object passed by the action "+ rlt.get(i)[2]+
						" is inconsistent with the type returned by the corresponding block function.");
			}
		}
	}
	
//	检查边是否只有一个hasTarget,一个hasSource	
	//删除没有target或者source的边
	public static void SysMLEdgeCheck(XMI xmi, OntoGenerator ont){
		String prefix = ont.getShortPrefix();
		String[] point={"Source","Target"};
		for(int i=0;i<point.length;i++){
			String rule="%s:SysMLEdge(?o) ^ %s:has%s(?o, ?s)^sqwrl:makeSet(?sset, ?s)^sqwrl:groupBy(?sset, ?o)^sqwrl:size(?n, ?sset)^swrlb:greaterThan(?n, 1) -> sqwrl:select(?o)";
			rule=String.format(rule, prefix,prefix,point[i]);
			List<String> rlt=ont.queryBySQWRL(rule, "o");
			if(rlt!=null){
				for(String r:rlt){
					rule="%s:has%s(%s,?n)^%s:hasNode(?d,?n)->sqwrl:select(?d,?n)";
					rule=String.format(rule, prefix,point[i],r,prefix);
					List<String[]> ans=ont.queryBySQWRL(rule, new String[]{"d","n"});
					StringBuffer message=new StringBuffer();
					message.append("Edge "+r+" have more than 1 "+point[i].toLowerCase()+": ");
					for(String[] a:ans){
						message.append(a[0]+":"+a[1]+" ");
					}
					MyLogger.LOGGER.error(message.toString());
				}
			}
		}
		
	}
}
