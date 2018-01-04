package edu.amss.fca.Method;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.amss.fca.Tools.OWLAPI_tools;
import edu.amss.fca.Tools.Trie;
import fr.labri.galatea.Attribute;
import fr.labri.galatea.BinaryAttribute;
import fr.labri.galatea.Concept;
import fr.labri.galatea.ConceptOrder;
import fr.labri.galatea.Context;
import fr.labri.galatea.Entity;
import fr.labri.galatea.algo.Algorithm;
import fr.labri.galatea.algo.SimpleGSH;
import fr.labri.galatea.algo.SimpleGSH2;
import gov.nih.nlm.nls.lvg.Api.NormApi;
import gov.nih.nlm.nls.lvg.Lib.LexItem;

public class Token_based_lattice_anchors {
	public static Map<String, String> label_origin_map = new HashMap<>();
	public static Set<String> string_equal_anchros = new HashSet<>();

	public static Set<String> get_Lexical_Anchors(OWLAPI_tools Onto1, OWLAPI_tools Onto2) throws Exception {

		ArrayList<OWLAPI_tools> Ontologies = new ArrayList<>();
		Ontologies.add(Onto1);
		Ontologies.add(Onto2);

		/**������**/
		ConceptOrder o = ConstructLattice(Ontologies);

		Set<String> Alignment_set = new HashSet<>();

		// �ҵ��Ȱ���AҲ����B��formal concepts����Ϊ����S
		Set<Concept> Set_S = new HashSet<>();

		for (Concept cc : o) {

			Set<Entity> Extent_Set = new HashSet<>();
			Extent_Set = cc.getExtent();

			if (Extent_Set.size() > 0 && cc.getIntent().size() > 0) {
				Set<Entity> entity_ori = new HashSet<>();
				for (Iterator<Entity> iter = Extent_Set.iterator(); iter.hasNext();) {
					String eString = iter.next().getName();
					String ori = label_origin_map.get(eString);
					Entity e = new Entity(ori);
					entity_ori.add(e);
				}
				cc.setExtent_Ori(entity_ori);

				Set<Entity> Generator = new HashSet<>();
				Generator = cc.getSimplifiedExtent();
				if (Check_A_B(entity_ori)) {
					Set_S.add(cc);
					/** �ҳ�����extent�Ļ�������1,�Ҷ����������������������formal concept **/
					if (Generator.size() > 1) {
						Set<String> A_B_ = Separate_Combine(Generator);
						Alignment_set.addAll(A_B_);
						string_equal_anchros.addAll(A_B_);
					}
					if (entity_ori.size() == 2) {
						Set<String> A_B_ = Separate_Combine(entity_ori);
						Alignment_set.addAll(A_B_);
					}
				}
			}
		}
		return Alignment_set;
	}

	public static boolean checkName(String string) {
		boolean flag = true;
		String prefix = null;
		int length = string.length();
		if (length > 5) {
			if (string.length() < 10) {
				prefix = string.substring(0, 5);
			} else {
				prefix = string.substring(0, 9);
			}
			if (prefix.contains("Orphanet_") || prefix.contains("DOID_") || prefix.substring(0, 3).contains("HP_")
					|| prefix.substring(0, 3).contains("MP_") || prefix.contains("NCI_C") || prefix.contains("MA_00")) {
				flag = false;
			}
		}
		return flag;
	}

	public static ConceptOrder ConstructLattice(ArrayList<OWLAPI_tools> Ontologies) throws Exception {
		long tic=System.currentTimeMillis();
		Context c = new Context();
		ArrayList<String> AllConcepts = new ArrayList<>(); // ������������(����)ǰ׺�ĸ���
		ArrayList<String> Pre_AllConcepts = new ArrayList<>(); // ��������(��)ǰ׺�ĸ���
		String[] pre = { "A_", "B_" };

		ArrayList<Entity> Entities = new ArrayList<Entity>();
		Map<String, Set<String>> Concept_AnnoationsSet_Map = new HashMap<>();

		for (int i = 0; i < Ontologies.size(); i++) {

			OWLAPI_tools Onto = Ontologies.get(i);

			Concept_AnnoationsSet_Map = Onto.getConcept_AllAnnoationsSet();

			/** ��������ӵ�context c ��!!!!!! **/
			for (String key : Concept_AnnoationsSet_Map.keySet()) {
				String pre_c = pre[i] + key;
				label_origin_map.put(pre_c, pre_c);
				if (checkName(key)) {
					AllConcepts.add(key);
					Pre_AllConcepts.add(pre_c);
					Entity e = new Entity(pre_c);
					Entities.add(e);
					c.addEntity(e);
				}
				Set<String> alternatives = Concept_AnnoationsSet_Map.get(key);//��ÿ�������alternative namesҲ��������һ������
				for (String alternative : alternatives) {
					AllConcepts.add(alternative);
					String pre_la = pre[i] + alternative;
					Pre_AllConcepts.add(pre_la);
					Entity E = new Entity(pre_la);
					Entities.add(E);
					c.addEntity(E);
					if (Concept_AnnoationsSet_Map.keySet().contains(alternative)) {
						continue;
					} else {
						label_origin_map.put(pre_la, pre_c);
					}
				}
			}
		}

		int e_num = Entities.size();

		String lvgConfigFile = "Lexical_Tools/data/config/lvg.properties";
		NormApi norm = new NormApi(lvgConfigFile);
		Trie trie = new Trie();
		String[] temp = null;
		// ��ÿ��������tokens������
		Map<String, String[]> AllConcepts_tokens = new HashMap<String, String[]>();
		/** ��������ӵ�context c ��!!!!!! **/
		Map<String, Attribute> Attr = new HashMap<>();
		Set<String> num_tokens = new HashSet<>();
		for (int i = 0; i < AllConcepts.size(); i++) {
			LexItem items = new LexItem(AllConcepts.get(i));
			Vector<LexItem> listStr = norm.Mutate(items);
			/*if(listStr.size()>=2)
				System.out.println("true");
			for(LexItem a:listStr)
			{
				System.out.println(a.GetTargetTerm());
			}*/
			if (listStr.size() == 0) 
			{
				String temp_findStem[] = new String[1];
				temp_findStem[0] = Tool_functions.tokeningWord(AllConcepts.get(i).toLowerCase());
				AllConcepts_tokens.put(Pre_AllConcepts.get(i), temp_findStem);
			} 
			else 
			{
				String normed = listStr.get(listStr.size() - 1).GetTargetTerm();
				temp = normed.split(" ");
				String temp_findStem[] = new String[temp.length];
				for (int j = 0; j < temp.length; j++) 
				{
					String a = temp[j];
					temp_findStem[j] = a;
					if ((a != null) && (Tool_functions.hasDigit(a))) 
					{
						Attribute attr = new BinaryAttribute(a);
						Attr.put(a, attr);
						c.addAttribute(attr);
						num_tokens.add(a);
					} else if ((a != null) && (!Tool_functions.hasDigit(a))) 
					{
						trie.addWord(a);
					}
				}
				AllConcepts_tokens.put(Pre_AllConcepts.get(i), temp_findStem);
			}
		}
		norm.CleanUp();

		List<String> attrs = new ArrayList<String>();
		attrs = trie.listAllWords();

		Set<String> delete_attr = new HashSet<>();
		for (String attr : attrs) 
		{
			//System.out.println(attr);
			Attribute a = new BinaryAttribute(attr);
			Attr.put(attr, a);
			c.addAttribute(a);
			if (trie.countWords(attr) > 7000) {
				delete_attr.add(attr);
			}
		}

		/** ��pair��ӵ�context c ��!!!!!! **/
		// ��ȡͬ���map
		Map<String, Set<String>> Syn_Map = new HashMap<>();// �洢token�Լ�����ͬ��ʣ�����Щͬ�����trie���д���
		String syn_path = "dict/Syn_Deriva.csv";
		BufferedReader Synonyms = new BufferedReader(new FileReader(new File(syn_path)));
		String line = null;
		String key_value[] = null;
		NormApi norm1 = new NormApi(lvgConfigFile);
		while ((line = Synonyms.readLine()) != null) {
			key_value = line.split("==");
			Set<String> syns_temp = new HashSet<>();
			String key = key_value[0];
			String value[] = key_value[1].split(",");
			for (int i = 0; i < value.length; i++) {
				syns_temp.add(value[i].trim());
			}
			Syn_Map.put(key, syns_temp);
		}
		Synonyms.close();
		norm1.CleanUp();

		String[] tokens = null;
		Set<String> attrs_set = new HashSet<>();
		attrs_set.addAll(attrs);
		for (int i = 0; i < AllConcepts.size(); i++) {
			tokens = AllConcepts_tokens.get(Pre_AllConcepts.get(i));
			/** ��trie���в���tokens,������ʡʱ��,��ʹ��wordnet,������ͬ��� **/
			for (int m = 0; m < tokens.length; m++) {
				String token = tokens[m];
				c.addPair(Entities.get(i), Attr.get(token));// �ڸ�token��Ӧ�����Ի���
				if (!Tool_functions.hasDigit(token)) {
					Set<String> syn = Syn_Map.get(token);
					if (syn != null) {
						for (String string : syn) {
							if (Attr.get(string) != null) {
								c.addPair(Entities.get(i), Attr.get(string));
							}
						}
					}
				}
			}
		}

		/** ���˶���ɾ�����е���������Ҳֻ������һ������Ķ��� **/
		for (int i = 0; i < e_num; i++) {
			Entity e = Entities.get(i);
			Set<Attribute> attr = c.getAttributes(e);
			ArrayList<Attribute> attr_list = new ArrayList<>();
			attr_list.addAll(attr);
			int attr_size = attr_list.size();
			int[] obj_num = new int[attr_size];
			boolean flag = true;
			for (int j = 0; j < attr_size; j++) {
				Attribute attribute = attr_list.get(j);
				Set<Entity> e_temp = c.getEntities(attribute);
				obj_num[j] = e_temp.size();
				if (obj_num[j] == 1) {
					continue;
				} else {
					flag = false;
				}
			}
			if (flag == true) {
				c.removeEntity(e);
				for (int j = 0; j < attr_size; j++) {
					c.removeAttribute(attr_list.get(j));
				}
			}
		}
		for (String attr : delete_attr) {
			Attribute attribute = new BinaryAttribute(attr);
			c.removeAttribute(attribute);
		}

		long toc=System.currentTimeMillis();
		System.out.println("The time of constructing lattice is "+ (toc-tic)/1000+"  s");
		System.out.println("���Եĸ���Ϊ�� "+c.getAttributes().size());
		System.out.println("ʵ��ĸ���Ϊ�� "+c.getEntities().size());
		/** ����� **/
		/*Algorithm a = new SimpleGSH(c);
		a.compute();
		ConceptOrder o = a.getConceptOrder();*/
		tic=System.currentTimeMillis();
		Algorithm a = new SimpleGSH2(c);
		a.compute();
		ConceptOrder o = a.getConceptOrder();
		toc=System.currentTimeMillis();
		System.out.println("The time of computing context is "+ (toc-tic)/1000+"  s");
		return o;
	}

	/** ���extent�еĸ����Ƿ�ͬʱ����A��B�����Ƿ����������������ĸ��� **/
	public static boolean Check_A_B(Set<Entity> Concepts_Set) {
		int A_num = 0;
		int B_num = 0;
		boolean flag = false;
		for (Iterator<Entity> iter = Concepts_Set.iterator(); iter.hasNext();) {

			String tem = iter.next().toString();
			if (tem.substring(0, 2).contains("A_")) {
				A_num++;
			} else if (tem.substring(0, 2).contains("B_")) {
				B_num++;
			}
		}
		if (A_num != 0 && B_num != 0) {
			flag = true;
		}
		return flag;
	}

	public static Set<String> Separate_Combine(Set<Entity> Concepts_Set) {
		Set<String> alignment = new HashSet<>();
		Set<String> A_concepts = new HashSet<>();
		Set<String> B_concepts = new HashSet<>();
		for (Iterator<Entity> iter = Concepts_Set.iterator(); iter.hasNext();) {

			String tem = iter.next().toString();
			if (tem.substring(0, 2).contains("A_")) {
				String origin = label_origin_map.get(tem);
				A_concepts.add(origin);

			} else if (tem.substring(0, 2).contains("B_")) {
				String origin = label_origin_map.get(tem);
				B_concepts.add(origin);

			}
		}
		if ((A_concepts.size() != 0) && (B_concepts.size() != 0)) {
			for (Iterator<String> iter1 = A_concepts.iterator(); iter1.hasNext();) {
				String aString = iter1.next();
				for (Iterator<String> iter2 = B_concepts.iterator(); iter2.hasNext();) {
					alignment.add(aString + " = " + iter2.next());
				}
			}
		}
		return alignment;
	}

	/** �ж�һ���ַ������Ƿ�������� **/
	public static boolean hasDigit(String content) {
		boolean flag = false;
		Pattern p = Pattern.compile(".*\\d+.*");
		Matcher m = p.matcher(content);
		if (m.matches())
			flag = true;
		return flag;
	}

	public static boolean check_classname(String name) {
		boolean flag = false;
		String prefix = name.substring(0, 5);
		if (prefix.contains("MA_0") || prefix.contains("NCI_C")) {
			flag = true;
		}
		return flag;
	}

	public static String tokeningWord(String str) {
		String s1 = str;
		String ss = "";
		for (int i = 0; i < s1.length() - 1; i++) {
			char aa = s1.charAt(i + 1);
			char a = s1.charAt(i);
			if (Character.isUpperCase(a) && i == 0)// �������ĸ�Ǵ�д��ֱ�����
			{
				ss = ss + String.valueOf(a);
			} else if (Character.isUpperCase(a) && Character.isLowerCase(aa) && i != 0)// �������ĸ�Ǵ�д����Ҫ����ָ���
			{
				ss = ss + " " + String.valueOf(a);
			} else if ((a == '-' || a == '_') || a == '.' || a == ',')// �������ַ�"-","_"
			// ���Һ���aa�Ǵ�д����������
			{
				// continue;
				ss = ss + " ";// ���ڼ�ӽ�'_','-'�������滻
			} else if (Character.isUpperCase(a) && Character.isUpperCase(aa)) {
				ss = ss + String.valueOf(a);
			} else if (Character.isLowerCase(a) && Character.isUpperCase(aa))// ǰ��Сд����Ӵ�д
			{
				ss = ss + String.valueOf(a) + " ";
			} else // ��ʵ����������
			{
				ss = ss + String.valueOf(a);
			}
		}
		ss = ss + s1.charAt(s1.length() - 1);
		ss = ss.replace("  ", " ").trim();
		return ss.toLowerCase().replaceAll("_|-", "");
	}
}
