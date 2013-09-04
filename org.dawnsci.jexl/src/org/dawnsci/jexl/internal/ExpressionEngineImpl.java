package org.dawnsci.jexl.internal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;
import org.apache.commons.jexl2.Script;
import org.dawb.common.services.expressions.ExpressionEngineEvent;
import org.dawb.common.services.expressions.IExpressionEngine;
import org.dawb.common.services.expressions.IExpressionEngineListener;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.dataset.Image;
import uk.ac.diamond.scisoft.analysis.dataset.Maths;

public class ExpressionEngineImpl implements IExpressionEngine{
	
	private JexlEngine jexl;
	private Expression expression;
	private MapContext context;
	private HashSet<IExpressionEngineListener> expressionListeners;
	private Job job;
	private Callable<Object> callable;
	
	public ExpressionEngineImpl() {
		//Create the Jexl engine with the DatasetArthmetic object to allows basic
		//mathematical calculations to be performed on AbstractDatasets
		jexl = new JexlEngine(null, new DatasetArithmetic(false),null,null);

		//Add some useful functions to the engine
		Map<String,Object> funcs = new HashMap<String,Object>();
		funcs.put("dnp", Maths.class);
		funcs.put("dat", JexlGeneralFunctions.class);
		funcs.put("im", Image.class);
		//TODO determine which function classes should be loaded as default
//		funcs.put("fft", FFT.class);
//		funcs.put("plt", SDAPlotter.class);
		jexl.setFunctions(funcs);
		
		expressionListeners = new HashSet<IExpressionEngineListener>();
	}
	

	@Override
	public void createExpression(String expression) throws Exception {
		this.expression = jexl.createExpression(expression);
	}

	@Override
	public Object evaluate() throws Exception {
		
		checkAndCreateContext();
		
		return expression.evaluate(context);
	}
	
	@Override
	public void addLoadedVariables(Map<String, Object> variables) {
		if (context == null) {
			context = new MapContext(variables);
			return;
		}
		
		for (String name : variables.keySet()) {
			context.set(name, variables.get(name));
		}
	}
	@Override
	public void addLoadedVariable(String name, Object value) {
		checkAndCreateContext();
		context.set(name, value);
	}

	@Override
	public Map<String, Object> getFunctions() {
		return jexl.getFunctions();
	}

	@Override
	public void setFunctions(Map<String, Object> functions) {
		jexl.setFunctions(functions);
	}

	@Override
	public void setLoadedVariables(Map<String, Object> variables) {
		context = new MapContext(variables);
	}

	@Override
	public Set<List<String>> getVariableNamesFromExpression() {
		try {
		final Script script = jexl.createScript(expression.getExpression());
		return script.getVariables();
		} catch (Exception e){
			return null;
		}
	}
	
	private void checkAndCreateContext() {
		if (context == null) {
			context = new MapContext();
		}
	}


	@Override
	public void addExpressionEngineListener(IExpressionEngineListener listener) {
		expressionListeners.add(listener);
	}


	@Override
	public void removeExpressionEngineListener(
			IExpressionEngineListener listener) {
		expressionListeners.remove(listener);
	}


	@Override
	public void evaluateWithEvent(final IProgressMonitor monitor) {
		if (expression == null) return;

		final Script script = jexl.createScript(expression.getExpression());

		checkAndCreateContext();

		callable = script.callable(context);

		final ExecutorService service = Executors.newCachedThreadPool();

		if (job == null) {
			job = new Job("Expression Calculation") {

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					String exp = expression.getExpression();
					try {
						
						if (monitor == null) {
							monitor = new NullProgressMonitor();
						}
						
						Future<Object> future = service.submit(callable);

						while (!future.isDone() && !monitor.isCanceled()) {
							Thread.sleep(100);
						}

						if (future.isDone()) {
							Object result = future.get();
							ExpressionEngineEvent event = new ExpressionEngineEvent(ExpressionEngineImpl.this, result, exp);
							fireExpressionListeners(event);
							return Status.OK_STATUS;
						}

					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (Exception e) {
						ExpressionEngineEvent event = new ExpressionEngineEvent(ExpressionEngineImpl.this, e, exp);
						fireExpressionListeners(event);
					}
					
					ExpressionEngineEvent event = new ExpressionEngineEvent(ExpressionEngineImpl.this, null, exp);
					fireExpressionListeners(event);

					return Status.CANCEL_STATUS;
				}
				
			};

		}
		job.schedule();
	}

	private void fireExpressionListeners(ExpressionEngineEvent event) {
		for (IExpressionEngineListener listener : expressionListeners){
			listener.calculationDone(event);
		}
	}

	@Override
	public Object getLoadedVariable(String name) {
		if (context == null) return null;
		return context.get(name);
	}
}
