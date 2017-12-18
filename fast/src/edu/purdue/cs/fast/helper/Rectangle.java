/**
 * Copyright Jul 5, 2015
 * Author : Ahmed Mahmood
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.purdue.cs.fast.helper;

public class Rectangle {
	
	private Point min ;
	private Point max;
	
	
	public Rectangle(Point min, Point max) {
		
		this.min = min;
		this.max = max;
	}
	public Point getMin() {
		return min;
	}
	public void setMin(Point min) {
		this.min = min;
	}
	public Point getMax() {
		return max;
	}
	public void setMax(Point max) {
		this.max = max;
	}
	@Override
	public String toString(){
		String output ="";
		output = "Min : "+getMin().toString()+", "+"Max : "+getMax().toString();
		return output;
	}
	@Override 
	public boolean equals(Object o){
		  if (o == this) 
	            return true;
	        if (!(o instanceof Rectangle)) 
	            return false;
	        Rectangle c = (Rectangle) o;
	        return (c.getMin().equals(this.getMin())  	&&c.getMax().equals(this.getMax())	);
	}
	
	
}
