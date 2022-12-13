package com.value.verifier;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.bean.onto.OntoGenerator;
import com.logger.MyLogger;

public class ValueVerifier {

	static OntoGenerator vg = new OntoGenerator();
	static Set<String> flag = new HashSet<>();
	static String[] items = { "ObjectType", "Do", "Effect", "Entry", "Exit", "Guard", "Trigger" };
	static String[] rules = {
			"%s:SysMLEdge(?e)^%s:SysMLNode(%s)^%s:SysMLNode(%s)^%s:hasSource(?e,%s)^%s:hasTarget(?e,%s)^%s:SysMLOtherItem(?o)^%s:hasOtherItem(?e,?o)^%s:hasValue(?o,\"%s\")->sqwrl:select(?e,?o)",
			"%s:SysMLEdge(?e)^%s:SysMLNode(%s)^%s:SysMLNode(%s)^%s:hasSource(?e,%s)^%s:hasTarget(?e,%s)^%s:SysMLOtherItem(?o)^%s:hasOtherItem(%s,?o)^%s:hasValue(?o,\"%s\")->sqwrl:select(?e,?o)",
			"%s:SysMLNode(%s)^%s:SysMLEdge(%s)^%s:SysMLNode(?t)^%s:hasSource(%s,%s)^%s:hasTarget(%s,?t)^%s:SysMLOtherItem(?o)^%s:hasOtherItem(%s,?o)^%s:hasValue(?o,\"%s\")->sqwrl:select(?t,?o)",
			"%s:SysMLNode(%s)^%s:SysMLEdge(%s)^%s:SysMLNode(?t)^%s:hasSource(%s,%s)^%s:hasTarget(%s,?t)^%s:SysMLOtherItem(?o)^%s:hasOtherItem(%s,?o)^%s:hasValue(?o,\"%s\")->sqwrl:select(?t,?o)",
			"%s:SysMLEdge(%s)^%s:SysMLNode(?s)^%s:SysMLNode(%s)^%s:hasSource(%s,?s)^%s:hasTarget(%s,%s)^%s:SysMLOtherItem(?o)^%s:hasOtherItem(%s,?o)^%s:hasValue(?o,\"%s\")->sqwrl:select(?s,?o)",
			"%s:SysMLEdge(%s)^%s:SysMLEdge(%s)^%s:SysMLNode(?n)^%s:hasTarget(%s,?n)^%s:hasSource(%s,?n)^%s:SysMLOtherItem(?o)^%s:hasOtherItem(%s,?o)^%s:hasValue(?o,\"%s\")->sqwrl:select(?n,?o)" };

	// Algorithm 1 Value verification and Labeling
	public static void valueVerify(OntoGenerator g) {
		vg = g;
		Set<String> values = g.getIndividualsOfClass("HumanValue");
		System.out.println("There are " + values.size() + " values to verify...");
		MyLogger.LOGGER.info("There are " + values.size() + " values to verify...");
		for (String value : values) {
			System.out.println("Value " + value + " is verifying, whose rules are following: ");
			MyLogger.LOGGER.info("Value " + value + " is verifying, whose rules are following: ");
			String type = g.getDataOfDataProperty(value, "RuleType").iterator().next();
			Set<String> contents = g.getDataOfDataProperty(value, "RuleValue");
			System.out.println(contents);
			Set<String> g_set = new HashSet<>();
			Set<String> b_set = new HashSet<>();
			switch (type) {
			case "sequence": {
				for (String content : contents) {
					Set<String> path = new HashSet<>();
					String[] omegas = content.split(" before ");
					for (int i = 0; i < omegas.length; i++)
						omegas[i] = omegas[i].trim();
					path=sequenceVerify(g.getShortPrefix() + ":" + omegas[0], g.getShortPrefix() + ":" + omegas[1]);
					if (path.size() != 0) {
						g_set.addAll(path);
						g_set.add(g.getShortPrefix() + ":" + omegas[0]);
						g_set.add(g.getShortPrefix() + ":" + omegas[1]);

					} else {
						b_set.add(g.getShortPrefix() + ":" + omegas[0]);
						b_set.add(g.getShortPrefix() + ":" + omegas[1]);
					}
				}
				improveSet(g_set);
				improveSet(b_set);
				logVerificationRlt(value, g_set, b_set);
				break;
			}
			case "necessity": {
				for (String content : contents) {
					String[] omegas = content.split(" need ");
					for (int i = 0; i < omegas.length; i++)
						omegas[i] = omegas[i].trim();

					Set<String> _g_set = new HashSet<>();
					String item = "";
					for (String i : items) {
						if (omegas[1].trim().startsWith(i)) {
							item = i;
							break;
						}
					}
					omegas[1] = omegas[1].replace(item + "(", "");
					omegas[1] = omegas[1].substring(0,omegas[1].length()-1);
					switch (item) {
					case "ObjectType": {
						_g_set = objectNeccesityVerify(g.getShortPrefix() + ":" + omegas[0], omegas[1]);
						break;
					}
					default:
						_g_set = otherItemNeccesityVerify(g.getShortPrefix() + ":" + omegas[0], omegas[1]);
					}

					if (_g_set.size() != 0) {
						g_set.addAll(_g_set);
						g_set.add(g.getShortPrefix() + ":" + omegas[0]);
						g_set.add(omegas[1]);
					} else {
						b_set.add(g.getShortPrefix() + ":" + omegas[0]);
						b_set.add(omegas[1]);
					}
				}
				improveSet(g_set);
				improveSet(b_set);
				logVerificationRlt(value, g_set, b_set);
				break;
			}
			case "mixture": {
				for (String content : contents) {
					String[] ruleValues = content.split("&&");
					String[] omegas = new String[3];
					omegas[0] = ruleValues[1].split(" before ")[0].trim();
					omegas[1] = ruleValues[1].split(" before ")[1].trim();
					omegas[2] = ruleValues[0].split(" need ")[1].trim();
					String item = "";
					for (String i : items) {
						if (omegas[2].startsWith(i)) {
							item = i;
							break;
						}
					}
					omegas[2] = omegas[2].replace(item + "(", "");
					omegas[2] = omegas[2].substring(0,omegas[2].length()-1);
					Set<String> _g_set = new HashSet<>();
					_g_set = mixtureVerify(g.getShortPrefix(), omegas, item);
					if (_g_set.size() != 0) {
						g_set.addAll(_g_set);
						g_set.add(g.getShortPrefix() + ":" + omegas[0]);
						g_set.add(g.getShortPrefix() + ":" + omegas[1]);
						g_set.add(omegas[2]);
					} else {
						b_set.add(g.getShortPrefix() + ":" + omegas[0]);
						b_set.add(g.getShortPrefix() + ":" + omegas[1]);
						b_set.add(omegas[2]);
					}
				}
				improveSet(g_set);
				improveSet(b_set);
				logVerificationRlt(value, g_set, b_set);
				break;
			}
			}
		}
	}

	private static Set<String> mixtureVerify(String shortPrefix, String[] omegas, String item) {
		String source = shortPrefix + ":" + omegas[0], target = shortPrefix + ":" + omegas[1];
		String rule;
		List<String[]> rlts = new ArrayList<>();
		Set<String> ans = new HashSet<>();
		if (vg.getDirectTypes(omegas[0]).contains("SysMLNode")) {
			if (vg.getDirectTypes(omegas[1]).contains("SysMLNode")) {
				switch (item) {
				case "ObjectType": {
					rule = String.format(rules[0], shortPrefix, shortPrefix, source, shortPrefix, target, shortPrefix,
							source, shortPrefix, target, shortPrefix, shortPrefix, shortPrefix, omegas[2]);
					rlts = vg.queryBySQWRL(rule, new String[] { "e", "o" });
					break;
				}
				default: {
					rule = String.format(rules[1], shortPrefix, shortPrefix, source, shortPrefix, target, shortPrefix,
							source, shortPrefix, target, shortPrefix, shortPrefix, target, shortPrefix, omegas[2]);
//					System.out.println(rule);
					rlts = vg.queryBySQWRL(rule, new String[] { "e", "o" });
				}
				}

			} else {
				rule = String.format(rules[2], shortPrefix, source, shortPrefix, target, shortPrefix, shortPrefix,
						target, source, shortPrefix, target, shortPrefix, shortPrefix, target, shortPrefix, omegas[2]);
//				System.out.println(rule);
				rlts = vg.queryBySQWRL(rule, new String[] { "t", "o" });
			}

		} else {
			if (vg.getDirectTypes(omegas[1]).contains("SysMLNode")) {
				switch (item) {
				case "ObjectType": {
					rule = String.format(rules[3], shortPrefix, source, shortPrefix, shortPrefix, target, shortPrefix,
							source, shortPrefix, source, target, shortPrefix, shortPrefix, source, shortPrefix,
							omegas[2]);
					rlts = vg.queryBySQWRL(rule, new String[] { "s", "o" });
					break;
				}
				default: {
					rule = String.format(rules[4], shortPrefix, source, shortPrefix, shortPrefix, target, shortPrefix,
							source, shortPrefix, source, target, shortPrefix, shortPrefix, target, shortPrefix,
							omegas[2]);
//					System.out.println(rule);
					rlts = vg.queryBySQWRL(rule, new String[] { "s", "o" });
				}
				}

			} else {
				rule = String.format(rules[5], shortPrefix, source, shortPrefix, target, shortPrefix, shortPrefix,
						source, shortPrefix, target, shortPrefix, shortPrefix, target, shortPrefix, omegas[2]);
//				System.out.println(rule);
				rlts = vg.queryBySQWRL(rule, new String[] { "n", "o" });
			}
		}
		for (String[] rlt : rlts) {
			ans.add(rlt[0]);
			ans.add(rlt[1]);
		}
		return ans;
	}

	private static void improveSet(Set<String> g_set) {

	}

	private static void logVerificationRlt(String value, Set<String> g_set, Set<String> b_set) {
		String info = "Value " + value + " verification done.";
		info += " g_set={";
		for (String rlt : g_set)
			info += rlt + ",";
		info += "}, b_set={";
		for (String rlt : b_set)
			info += rlt + ",";
		info += "}.";
		MyLogger.LOGGER.info(info);
		System.out.println(info);
	}

	@SuppressWarnings("unchecked")
	private static Set<String> otherItemNeccesityVerify(String subject, String itemValue) {
		String prefix = vg.getShortPrefix();
		String rule = "%s:SysMLOtherItem(?other)^%s:hasValue(?other,\"%s\")^%s:hasOtherItem(%s,?other)->sqwrl:select(?other)";
		rule = String.format(rule, prefix, prefix, itemValue, prefix, subject);
		return (Set<String>) vg.queryBySQWRL(rule, "other");
	}

	private static Set<String> objectNeccesityVerify(String subject, String type) {
		String prefix = vg.getShortPrefix();
		Set<String> rlt = new HashSet<>();
		String rule = "%s:SysMLEdge(?e)^%s:hasTarget(?e,%s)^%s:hasObject(?e,?o)^%s:hasValueType(?o,\"%s\")->sqwrl:select(?e,?o)";
		rule = String.format(rule, prefix, prefix, subject, prefix, prefix, type);
//		System.out.println(rule);
		List<String[]> rlts = vg.queryBySQWRL(rule, new String[] { "e", "o" });
		for (String[] eo : rlts) {
			rlt.add(eo[0]);
			rlt.add(eo[1]);
		}
		return rlt;
	}

	private static Set<String> sequenceVerify(String source, String target) {
		Set<String> rlt = new HashSet<>();
		Set<String> path=new HashSet<>();
		System.out.println("Searching path...");
		dfs(source, target, rlt , path);
		System.out.println("Done.");
		return path;
	}

	private static void dfs(String source, String target, Set<String> rlt, Set<String> path) {
		if (source.equals(target)) {
			for(String r:rlt)
				path.add(r);
			return;
		}
		String prefix = vg.getShortPrefix();
		String rule = String.format("%s:SysMLEdge(?e)^%s:hasSource(?e,%s)^%s:hasTarget(?e,?a)->sqwrl:select(?a)",
				prefix, prefix, source, prefix);
		List<String> targets = vg.queryBySQWRL(rule, "a");
		for (String t : targets) {
			if (!flag.contains(t)) {
//				System.out.println(t);
				flag.add(t);
				rlt.add(t);
				dfs(t, target, rlt, path);
				rlt.remove(t);
			}
		}
		return;
	}
}
