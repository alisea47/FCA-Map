package edu.amss.fca.Method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.amss.fca.Tools.OWLAPI_tools;
import fr.labri.galatea.Attribute;
import fr.labri.galatea.BinaryAttribute;
import fr.labri.galatea.Concept;
import fr.labri.galatea.ConceptOrder;
import fr.labri.galatea.Context;
import fr.labri.galatea.Entity;
import fr.labri.galatea.algo.Algorithm;
import fr.labri.galatea.algo.SimpleGSH;
import fr.labri.galatea.algo.SimpleGSH2;

public class Structural_based_lattice_screen {
/*	public static Map<String, Set<String>> map_support = new HashMap<>();//key:ƥ���; value:֧�ָ�ƥ��Ե�����ƥ���
	public static Map<String, Set<String>> map_unsupport = new HashMap<>();//key:ƥ���; value:��֧�ָ�ƥ��Ե�����ƥ���
	public static Map<String, Set<String>> map_negative = new HashMap<>();//key:ƥ���; value:��Ϊdisjoint-with��֧�ֵ�ƥ���
	public static Context c = new Context();
	public static ConceptOrder conceptOrder;*/
	
	public static Map<String, Set<String>> map_support;//key:ƥ���; value:֧�ָ�ƥ��Ե�����ƥ���
	public static Map<String, Set<String>> map_unsupport ;//key:ƥ���; value:��֧�ָ�ƥ��Ե�����ƥ���
	public static Map<String, Set<String>> map_negative ;//key:ƥ���; value:��Ϊdisjoint-with��֧�ֵ�ƥ���
	public static Context c ;
	public static ConceptOrder conceptOrder;

	public Structural_based_lattice_screen() {
		// TODO Auto-generated constructor stub
	}

	/**����positive��Ϣ��ɸѡ:ISA, PAT-OF,Sibling-With**/
	public static Set<String> get_Repaired_Anchors(ArrayList<OWLAPI_tools> Ontologies,
			Map<String, Map<String, Set<String>>> structure_properity, Set<String> Correspondences_initial_set,
			Set<String> string_equal_anchors) throws Exception {
		
		map_support = new HashMap<>();//key:ƥ���; value:֧�ָ�ƥ��Ե�����ƥ���
		map_unsupport = new HashMap<>();//key:ƥ���; value:��֧�ָ�ƥ��Ե�����ƥ���
		map_negative = new HashMap<>();//key:ƥ���; value:��Ϊdisjoint-with��֧�ֵ�ƥ���
		long tic=System.currentTimeMillis();
		c = new Context();
		//public static ConceptOrder conceptOrder;
		
		
		int pre_num = Correspondences_initial_set.size();

		ArrayList<String> Correspondences_initial = new ArrayList<>();
		Correspondences_initial.addAll(Correspondences_initial_set);
		/** ��������ӵ�context c ��!!!!!! **/
		ArrayList<Attribute> Attr_SubclassOf = new ArrayList<>();
		ArrayList<Attribute> Attr_Dis_ISA = new ArrayList<>();
		ArrayList<Attribute> Attr_ISA_Dis = new ArrayList<>();
		ArrayList<Attribute> Attr_SiblingWith = new ArrayList<>();
		ArrayList<Attribute> Attr_PartOf = new ArrayList<>();
		ArrayList<Attribute> Attr_SupclassOf = new ArrayList<>();
		for (int i = 0; i < pre_num; i++) {
			Attribute a1 = new BinaryAttribute("(ISA)" + Correspondences_initial.get(i));
			Attribute a2 = new BinaryAttribute("(D_I)" + Correspondences_initial.get(i));
			Attribute a3 = new BinaryAttribute("(I_D)" + Correspondences_initial.get(i));
			Attribute a4 = new BinaryAttribute("(Sib)" + Correspondences_initial.get(i));
			Attribute a5 = new BinaryAttribute("(PAT)" + Correspondences_initial.get(i));
			Attribute a6 = new BinaryAttribute("(SUP)" + Correspondences_initial.get(i));
			Attr_SubclassOf.add(a1);
			Attr_Dis_ISA.add(a2);
			Attr_ISA_Dis.add(a3);
			Attr_SiblingWith.add(a4);
			Attr_PartOf.add(a5);
			Attr_SupclassOf.add(a6);
			c.addAttribute(a1);
			c.addAttribute(a2);
			c.addAttribute(a3);
			c.addAttribute(a4);
			c.addAttribute(a5);
			c.addAttribute(a6);
		}

		/** ���õ����ఴA_��B_�ֿ� **/
		String[] temp = null;
		ArrayList<String> A_Concepts = new ArrayList<>();
		ArrayList<String> B_Concepts = new ArrayList<>();
		Set<String> All_concepts = new HashSet<>();
		Map<String, ArrayList<String>> corres_map = new HashMap<>();//key:����C; value:��Cƥ������и���
		for (int i = 0; i < pre_num; i++) {
			temp = Correspondences_initial.get(i).split(" = ");
			String aString = temp[0];
			String bString = temp[1];
			All_concepts.add(aString);
			All_concepts.add(bString);
			if (corres_map.keySet().contains(aString)) {
				ArrayList<String> as = corres_map.get(aString);
				as.add(bString);
			} else {
				ArrayList<String> as = new ArrayList<>();
				as.add(bString);
				corres_map.put(aString, as);
			}
			if (corres_map.keySet().contains(bString)) {
				ArrayList<String> as = corres_map.get(bString);
				as.add(aString);
			} else {
				ArrayList<String> as = new ArrayList<>();
				as.add(aString);
				corres_map.put(bString, as);
			}
			if (temp[0].substring(0, 2).contains("A_")) {
				A_Concepts.add(temp[0].substring(2));
				B_Concepts.add(temp[1].substring(2));
			} else {
				A_Concepts.add(temp[1].substring(2));
				B_Concepts.add(temp[0].substring(2));
			}
		}
		ArrayList<ArrayList<String>> initial_match = new ArrayList<>();
		initial_match.add(A_Concepts);
		initial_match.add(B_Concepts);

		// �������Դ�����е����и���
		String[] pre = { "A", "B" };
		Tool_functions tool_functions = new Tool_functions();
		for (int i = 0; i < Ontologies.size(); i++) 
		{
			OWLAPI_tools Onto = Ontologies.get(i);
			ArrayList<String> concepts = Onto.getConcepts();
			ArrayList<String> pre_concepts = new ArrayList<>();
			ArrayList<Entity> entities = new ArrayList<Entity>();
			for (String concept : concepts) 
			{
				String pre_concept = pre[i] + "_" + concept;
				pre_concepts.add(pre_concept);
				Entity e = new Entity(pre_concept);
				entities.add(e);
				c.addEntity(e);
			}

			Map<String, Set<String>> Subclass_map = structure_properity.get("sub" + pre[i]);
			Map<String, Set<String>> Sib_map = structure_properity.get("sib" + pre[i]);
			Map<String, Set<String>> Partof_map = structure_properity.get("pat" + pre[i]);
			Map<String, Set<String>> Disjoint_map = structure_properity.get("dis" + pre[i]);

			for (int n = 0; n < concepts.size(); n++) {
				String concept = concepts.get(n);
				for (int m = 0; m < pre_num; m++) {
					String con = initial_match.get(i).get(m);
					/** ���SubClassOf�� **/
					Set<String> subclass = Subclass_map.get(con);
					if (subclass.contains(concept)) {
						c.addPair(entities.get(n), Attr_SubclassOf.get(m));
					}

					/** ���SuperClassOf�� **/
					Set<String> subclass_Object = Subclass_map.get(concepts.get(n));
					if (subclass_Object.contains(con)) {
						c.addPair(entities.get(n), Attr_SupclassOf.get(m));
					}

					/** ���SiblingWith�� **///(�о�������)
					Set<String> sibclasses = Sib_map.get(con);//100Ӧ����һ������ʽ��ֵ
					if (sibclasses.contains(concept) && sibclasses.size() < 100) {
						c.addPair(entities.get(n), Attr_SiblingWith.get(m));
					}
					
					/** ���PartOf�� **/
					Set<String> partclasses = Partof_map.get(concepts.get(n));
					if (partclasses.contains(con)) {
						c.addPair(entities.get(n), Attr_PartOf.get(m));
					}

					if (i == 0) {
						/** ���SubClassOf�� **/
						if (subclass.contains(concept)) {
							c.addPair(entities.get(n), Attr_ISA_Dis.get(m));
						}

						/** ���DisjointWith�� **/
						Set<String> Disjoint_with = Disjoint_map.get(con);
						if (Disjoint_with.contains(concept)) {
							c.addPair(entities.get(n), Attr_Dis_ISA.get(m));
						}
					} else {
						/** ���SubClassOf�� **/
						if (subclass.contains(concept)) {
							c.addPair(entities.get(n), Attr_Dis_ISA.get(m));
						}

						/** ���DisjointWith�� **/
						Set<String> Disjoint_with = Disjoint_map.get(con);
						if (Disjoint_with.contains(concept)) {
							c.addPair(entities.get(n), Attr_ISA_Dis.get(m));
						}
					}
				}
			}

		}

		/**�������ԺͶ���ɾ����Ϊ�յĶ������Ϊ�յ�����**/
		Set<Entity> Entities = new HashSet<>();
		Entities.addAll(c.getEntities());
		for (Entity e : Entities) {
			Set<Attribute> attributes = c.getAttributes(e);
			if (attributes.size() == 0) {
				c.removeEntity(e);
			}
		}

		Set<Attribute> Attributes = new HashSet<>();
		Attributes.addAll(c.getAttributes());
		for (Attribute a : Attributes) {
			Set<Entity> entities = c.getEntities(a);
			if (entities.size() == 0) {
				c.removeAttribute(a);
			}
		}

		/** �������� **/
		c.Clarify();
		long toc=System.currentTimeMillis();
		System.out.println("The time of constructing lattice is "+ (toc-tic)/1000+"  s");
		/** ������ **/
		//Algorithm a = new SimpleGSH(c);
		tic=System.currentTimeMillis();
		Algorithm a = new SimpleGSH2(c); //�̰߳汾
		a.compute();
		conceptOrder = a.getConceptOrder();
		toc=System.currentTimeMillis();
		System.out.println("The time of computing context is "+ (toc-tic)/1000+"  s");
		
		Set<String> filtered = new HashSet<>();//�洢����ɸѡ������ƥ���
		Set<Concept> Set_S = new HashSet<>();

		//ѭ��
		for (Concept cc : conceptOrder) {
			String intent = cc.getIntent().toString().replace("[", "").replace("]", "").trim();
			String extent = cc.getExtent().toString().replace("[", "").replace("]", "").trim();
			if (tool_functions.Check_A_B(cc.getExtent()) && cc.getIntent().size() != 0 && cc.getExtent().size() != 0) 
			{
				ArrayList<ArrayList<String>> A_B_ = tool_functions.Separate(extent);
				ArrayList<String> A_Class = A_B_.get(0);
				ArrayList<String> B_Class = A_B_.get(1);
				String Intent_split[] = null;
				Set<String> corres_positive = new HashSet<>();
				Set<String> corres_negative = new HashSet<>();
				Intent_split = intent.split(", ");
				//��intent�ı�ǩ���з����������disjoint��Ϣ��Ҫ�鵽negative��
				for (int i = 0; i < Intent_split.length; i++) {
					String prefix = Intent_split[i].substring(0, 5);
					String corre = Intent_split[i].substring(5);
					if (prefix.contains("I_D") || prefix.contains("D_I")) {
						corres_negative.add(corre);
					} else {
						corres_positive.add(corre);
					}
				}

				Set_S.add(cc);

				for (String aString : A_Class) 
				{
					ArrayList<String> bStrings = corres_map.get(aString);
					if (bStrings == null) 
					{
						continue;
					} 
					else 
					{
						for (String bString : bStrings) 
						{
							//String corr_ = aString + " = " + bString;
							if (B_Class.contains(bString))  //��֧�ּ���	
							{ 
								String corr_ = aString + " = " + bString;
								if (corres_positive.size() > 0) {
									tool_functions.add_sup_OR_unsup(corres_positive, map_support, corr_);
								}
								if (corres_negative.size() > 0) {									
									tool_functions.add_sup_OR_unsup(corres_negative, map_negative, corr_);
								}
							}
						}
					}
				}
			}
		}

		/**��negative������**/
		Map<String, Integer> map_negative_count = new HashMap<>();
		for (String key : map_negative.keySet()) {
			map_negative_count.put(key, map_negative.get(key).size());
		}
		Map<String, Set<String>> sorted_map_negative = new HashMap<>();
		Map<String, Integer> map_negative_count1 = new HashMap<>();
		ArrayList<String> ne_list = new ArrayList<>();
		map_negative_count1 = Tool_functions.sortMap1(map_negative_count);
		for (String s : map_negative_count1.keySet()) {
			Set<String> negative = map_negative.get(s);
			sorted_map_negative.put(s, negative);
			ne_list.add(s);
		}
		Set<String> delete = Support_closure.delete(map_negative, map_support);
		filtered.addAll(map_support.keySet());
		filtered.addAll(string_equal_anchors); //���ڳ���һ����ƥ�䣬�����֧�ּ�Ϊ�գ���ɾ����ֻɾ���ò�һ����
		filtered.removeAll(delete);
		/*ArrayList<String> delete_final = new ArrayList<>();
		delete_final.addAll(Correspondences_initial);
		delete_final.removeAll(filtered);*/
		return filtered;
	}
}
