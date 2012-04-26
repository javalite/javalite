/**
 *
Copyright 2012 Kalyan Mulampaka

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
package activejdbc.codegen;

/**
 * Class to represent a method in the class
 * @author Kalyan Mulampaka
 *
 */
public class Method
{
	private String name;
	private Parameter parameter;
	private boolean generateSetter = true;
	private boolean generateGetter = true;

	public Method ()
	{

	}

	public String getName ()
	{
		return name;
	}

	public void setName (String name)
	{
		this.name = name;
	}

	public Parameter getParameter ()
	{
		return parameter;
	}

	public void setParameter (Parameter parameter)
	{
		this.parameter = parameter;
	}

	public boolean isGenerateSetter ()
	{
		return generateSetter;
	}
	
	public void setGenerateSetter (boolean generateSetter)
	{
		this.generateSetter = generateSetter;
	}
	
	public boolean isGenerateGetter ()
	{
		return generateGetter;
	}
	
	public void setGenerateGetter (boolean generateGetter)
	{
		this.generateGetter = generateGetter;
	}



}
