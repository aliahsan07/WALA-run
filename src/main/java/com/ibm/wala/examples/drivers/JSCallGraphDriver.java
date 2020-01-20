package com.ibm.wala.examples.drivers;

import com.ibm.wala.cast.js.ipa.callgraph.JSCallGraphUtil;
import com.ibm.wala.cast.js.translator.CAstRhinoTranslatorFactory;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.examples.analysis.js.JSCallGraphBuilderUtil;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.ISSABasicBlock;
import com.ibm.wala.ssa.SSACFG;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.collections.Iterator2Iterable;
import com.ibm.wala.util.graph.dominators.Dominators;
import com.ibm.wala.util.graph.dominators.GenericDominators;
import com.ibm.wala.util.graph.traverse.DFS;
import com.ibm.wala.util.graph.traverse.DFSDiscoverTimeIterator;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Set;

public class JSCallGraphDriver {

	/**
	 * Usage: JSCallGraphDriver path_to_js_file
	 * //	 * @param args
	 *
	 * @throws WalaException
	 * @throws CancelException
	 * @throws IOException
	 * @throws IllegalArgumentException
	 */


	private static CGNode getFunctionNode(CallGraph CG, String dir, String file) {
		TypeName type = TypeName.findOrCreate("L" + dir + "/" + file);
		if (CG != null) {
			Iterator<CGNode> iter = CG.iterator();
			CGNode node;
			while (iter.hasNext()) {
				node = iter.next();
				TypeName tempType = node.getMethod().getDeclaringClass().getName();
				if (tempType.toString().equals("Lexample.js")) {
					return node;
				}
			}
		}
		System.err.println("Can't find :" + dir + "/" + file);
		return null;
	}

	public static void main(String[] args) throws IllegalArgumentException, IOException, CancelException, WalaException {
		Path path = Paths.get(args[0]);

		// Use Rhino to parse JavaScript
		JSCallGraphUtil.setTranslatorFactory(new CAstRhinoTranslatorFactory());
		// build the call graph
		CallGraph CG = JSCallGraphBuilderUtil.makeScriptCG(
				path.getParent().toString(), path.getFileName().toString());

		PointerAnalysis<InstanceKey> ptrAnalysis = JSCallGraphBuilderUtil.getPointerAnalysis(path.getParent().toString(), path.getFileName().toString());

//		System.out.println(CallGraphStats.getStats(CG));
//		System.out.println(CallGraphStats.getCGStats(CG));
//		System.out.println("=====================");


		for (CGNode node : CG) {

			TypeName tempType = node.getMethod().getDeclaringClass().getName();
			if (tempType.toString().contains("Lexample.js")) {


				// Get the IR of a CGNode
				IR ir = node.getIR();


				// Get CFG from IR
				SSACFG cfg = ir.getControlFlowGraph();


				System.out.println("===============================");
				System.out.println(node.getMethod());
				System.out.println(cfg.getMethod().getDeclaringClass().getName().toString());


				// Analysis for loop checking
				// Define dominator for each cfg

				System.err.println("loop analysis");


				Dominators dominator = new GenericDominators(cfg, cfg.getBasicBlock(0));
				dominator.make(CG, node);

				// DFS over basic blocks
				DFSDiscoverTimeIterator<ISSABasicBlock> iterator = DFS.iterateDiscoverTime(cfg);


				while (iterator.hasNext()) {
					ISSABasicBlock block = iterator.next();
//				System.out.println(block.getNumber());
					Iterator<ISSABasicBlock> joeRoot = cfg.getSuccNodes(block);
					while (joeRoot.hasNext()) {
						ISSABasicBlock nextBlock = joeRoot.next();
					// a back edge from the current block to a block that was prev discovered
						if (dominator.isDominatedBy(block, nextBlock)) {
							System.out.println("loooooooooooooop detected");
						}
					}

				}



				System.err.println("call graph analysis");

				Iterator<CGNode> succNodes = CG.getSuccNodes(node);

				for (CallSiteReference csRef: Iterator2Iterable.make(node.iterateCallSites())){
					Set<CGNode> possibleTargets = CG.getPossibleTargets(node, csRef);
					int numberOfTargets = CG.getNumberOfTargets(node, csRef);
					System.out.println("Number of targets: " + numberOfTargets);
					for (CGNode target: possibleTargets){
						System.out.println(target.getMethod());
						System.out.println("target found");
					}

				}


				// get the predecessors
				for (CGNode p : Iterator2Iterable.make(CG.getPredNodes(node))) {
//				System.out.println(",,,,,,,,,,,,,,");
//				System.out.println("Predeccesor: " + p);

					//
//				for (CallSiteReference csRef : Iterator2Iterable.make(p.iterateCallSites())) {
////					System.out.println("CHECKING FOR TARGET CALL SITES");
////					System.out.println(csRef.getDeclaredTarget());
////					System.out.println(csRef.getDeclaredTarget().getDescriptor());
//					Set<CGNode> targets = CG.getPossibleTargets(p, csRef);
//					int count = CG.getNumberOfTargets(p, csRef);
////					System.out.println("COUNT: " + count);
////					for (CGNode target: targets){
////						System.out.println("MATCHES");
////						System.out.println(target.getMethod());
////					}
////					System.out.println("Number of targets: "+  targets.size());
//				}
				}

				// Iterate over the Basic Blocks of CFG
				Iterator<ISSABasicBlock> cfgIt = cfg.iterator();

//			if (!cfg.getMethod().getDeclaringClass().getName().toString().equals("Lexample.js")){
//				continue;
//			}
				while (cfgIt.hasNext()) {
					ISSABasicBlock ssaBb = cfgIt.next();
					System.out.println("*****************************");
					System.out.println("Basic block #" + ssaBb.getNumber());
					// Iterate over SSA Instructions for a Basic Block
					Iterator<SSAInstruction> ssaIt = ssaBb.iterator();
					while (ssaIt.hasNext()) {
						SSAInstruction ssaInstr = ssaIt.next();
						//Print out the instruction
						System.out.println(ssaInstr);
					}
				}
				System.out.println("*****************************");
				System.out.println("\n");


				// Pointer Analysis
//			ptrAnalysis.getHeapModel().getPointerKeyForLocal(node, )



				System.err.println("pointer analysis");

//				ir.getLocalNames()
				IMethod m = node.getMethod();
				SSAInstruction[] instructions = ir.getInstructions();

				for (int i = 0; i < instructions.length; i++) {

					if (instructions[i] != null) {
						int ln = m.getLineNumber(i);

						for (int j = 0; j < instructions[i].getNumberOfDefs(); j++) {
							int def = instructions[i].getDef(j);
							System.err.println(("    looking at def " + j + ": " + def));
							String[] names = ir.getLocalNames(i, def);
							if (names != null) {
								for (String name : names) {
									System.err.println(("      looking at name " + name));

									PointerKey pointerKeyForLocal = ptrAnalysis.getHeapModel().getPointerKeyForLocal(node, def);
									pointerKeyForLocal.toString();
								}
							}
						}

					}
				}


			}



		}

	}
}