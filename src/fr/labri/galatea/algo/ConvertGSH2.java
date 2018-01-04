/*
   Copyright 2009 Jean-Rémy Falleri

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package fr.labri.galatea.algo;

import java.awt.event.MouseWheelEvent;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import fr.labri.galatea.Attribute;
import fr.labri.galatea.Concept;
import fr.labri.galatea.ConceptOrder;
import fr.labri.galatea.Context;
import fr.labri.galatea.Entity;

/**ɾ��һ����attribute����ԭ��GSH�ϸ��ģ�ʹ���Ϊɾ��attribute֮���context��Ӧ����lattice**/
public class ConvertGSH2 extends Algorithm {

	private ConceptOrder gsh;

	public ConvertGSH2(Context context) {
		super(context);
	}

	public void init_gsh(ConceptOrder conceptOrder) {
		gsh = conceptOrder;
	}

	public void compute(Set<Attribute> delete_attr, Map<Attribute, Concept> attr_attrconcept) {
		Set<Concept> AllChildren = new HashSet<>(); //�õ�����delete���Ե����Ը��������children
		Set<Concept> delete_attrconcept = new HashSet<>();
		for (Attribute attribute : delete_attr) {
			Concept attrconcept = attr_attrconcept.get(attribute);//�õ������Ե����Ը���
			Set<Concept> children = attrconcept.getAllChildren();//�õ������Ը��������children
			Set<Attribute> key_intent = attrconcept.getSimplifiedIntent();
			if (key_intent.size() == 1) { //���Ҫɾ����������Ը����key intentֻ����һ�����ԣ���ֱ��ɾ��
				gsh.removeConcept(attrconcept);
				AllChildren.addAll(children);
				delete_attrconcept.add(attrconcept);
			} else { //���Ҫɾ����������Ե����Ը����key intent������������				
//				intent.remove(attribute);
//				key_intent.remove(attribute);
				if (key_intent.size() == 0) {
					gsh.removeConcept(attrconcept);
					AllChildren.addAll(children);
					delete_attrconcept.add(attr_attrconcept.get(attribute));
				} else {
					AllChildren.addAll(children);
//					for (Concept child : children) {
//						child.getIntent().remove(attribute);
//					}
				}
			}
		}
		
		System.out.println(delete_attrconcept);
		AllChildren.removeAll(delete_attrconcept);//�õ�������delete���Ը����children���ϣ�
													//����һ��delete���Ը�������һ��delete���Ը���ĺ���
		System.out.println(AllChildren);
		
		for (Concept child : AllChildren) {
//			System.out.println("                       ");
//			System.out.println("==child: "+child);
			repalceConcept(child, delete_attr);

		}
//		Set<Concept> conceptss = gsh.getConcepts();
//		for (Concept concept : conceptss) {
//			System.out.println(concept.toString() + " \n&&chi: " + concept.getChildren().toString() + " \n&&par: "
//					+ concept.getParents().toString());
//		}

	}

	public void repalceConcept(Concept old_concept, Set<Attribute> delete_attr) {
		
		Concept new_concept = new Concept();
		new_concept.addExtent(old_concept.getExtent());
		Set<Attribute> attributes = new HashSet<>();
		attributes.addAll(old_concept.getIntent());
		attributes.removeAll(delete_attr);
		new_concept.addIntent(attributes);

		/**���磬���ڸ���A:([a, b, c, g, h],[3])����c�������Ը���B:([a,c],[3,4,6,7,8])��A��parent��
		 * ɾȥB֮��A��key_intent��Ϊc��(Ϊʲô��getSimplifiedIntent()����)����ʵ����Ӧ���ǿյģ�
		 * �����ֲ��ܸı�old������**/
		Set<Attribute> key_intent = new HashSet<>();
		key_intent.addAll(old_concept.getSimplifiedIntent());
		key_intent.removeAll(delete_attr);//Ҫɾȥkey_intent�е�ɾ�����ԣ�Ȼ���Ƿ����0
		
//		System.out.println(key_intent);
//		System.out.println(old_concept + " "+old_concept.getSimplifiedIntent());
//		System.out.println(old_concept.getChildren() + " "+old_concept.getParents());
		if (key_intent.size() > 0) {
			gsh.getConcepts().add(new_concept);
			gsh.delete_add_children_parents(old_concept, new_concept);
//			System.out.println("add: " + new_concept);
		} else {
			Set<Entity> entities_calculate = context.Attribute_derivation(attributes);
			Set<Entity> entities_ori = new_concept.getExtent();
			if (entities_calculate.equals(entities_ori)) {
//				System.out.println("^^^^");
				gsh.getConcepts().add(new_concept);
				gsh.delete_add_children_parents(old_concept, new_concept);
//				System.out.println("add: " + new_concept);
//				System.out.println(gsh.getConcepts());
			} 
		}
		gsh.removeConcept(old_concept);
//		System.out.println(gsh.getConcepts());
	}

	

	@Override
	public ConceptOrder getConceptOrder() {
		return gsh;
	}

	@Override
	public void compute() {
		// TODO Auto-generated method stub

	}

}
