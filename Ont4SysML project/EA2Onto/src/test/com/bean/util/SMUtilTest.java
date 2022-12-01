package test.com.bean.util;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import com.bean.UML.UMLTransition;
import com.bean.UML.XMI;
import com.util.ParseUtil;
import com.util.StmUtil;

public class SMUtilTest {

	@Test
	public void test() {
		XMI xmi = ParseUtil.parseXMI(new File("src\\resource\\autocar.xml"));
      List<UMLTransition> allTransitions = StmUtil.getAllTransitions(xmi);
      System.out.println(allTransitions.stream().map(UMLTransition::getSource).collect(Collectors.toList()));
	}

}
