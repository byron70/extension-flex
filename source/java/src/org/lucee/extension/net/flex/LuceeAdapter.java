/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 * Copyright (c) 2017, Lucee Assosication Switzerland
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 *
 **/
package org.lucee.extension.net.flex;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import org.lucee.extension.amf.caster.AMFCaster;
import org.lucee.extension.amf.caster.ClassicAMFCaster;

import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.engine.EngineChangeListener;
import lucee.runtime.util.Cast;
import flex.messaging.FlexContext;
import flex.messaging.config.ConfigMap;
import flex.messaging.messages.Message;
import flex.messaging.services.ServiceAdapter;

// FUTURE make this class independent from flex.messaging... so that the loader no longer need the flex jar

/**
 * Lucee implementation of the ServiceAdapter, forward all BlazeDS Request to the CFMLEngine.
 */
public class LuceeAdapter extends ServiceAdapter implements EngineChangeListener {

	public static final short LOWER_CASE=0;
	public static final short UPPER_CASE=1;
	public static final short ORIGINAL_CASE=2;

	private CFMLEngine engine;
	private ConfigMap properties;
	private BlazeDS util;
	private AMFCaster caster;

	@Override
	public void initialize(String id, ConfigMap properties) {
		super.initialize(id, properties);
        this.properties=properties;
        try{
	        // we call this because otherwse they does not exist (bug in BlazeDS)
	        ConfigMap propertyCases = properties.getPropertyAsMap("property-case", null);
	        if(propertyCases!=null){
	            propertyCases.getPropertyAsBoolean("force-cfc-lowercase", false);
	            propertyCases.getPropertyAsBoolean("force-query-lowercase", false);
	            propertyCases.getPropertyAsBoolean("force-struct-lowercase", false);
	        }
	        ConfigMap access = properties.getPropertyAsMap("access", null);
	        if(access!=null){
	            access.getPropertyAsBoolean("use-mappings", false);
	            access.getPropertyAsString("method-access-level","remote");
	        }

	        caster=getAMFCaster(propertyCases);

        }
        catch(Throwable t){}

    }



	@Override
	public Object invoke(Message message){
		try {
			if(util==null){
				util = new BlazeDS(caster);
			}
			return util.invoke(this,message);
		}
		catch (Exception e) {e.printStackTrace();
			throw new RuntimeException(e);
		}
    }

    /**
     * load (if needed) and return the CFMLEngine
     * @return CFML Engine
     */
    private CFMLEngine getEngine() {
    	if(engine==null){
        	try {CFMLEngineFactory.getInstance();
				engine=CFMLEngineFactory.getInstance(FlexContext.getServletConfig(),this);
			}
        	catch (Throwable e) {
				throw new RuntimeException(e);
			}
        }
    	return engine;
	}

    @Override
	public void onUpdate() {
        try {
            engine=CFMLEngineFactory.getInstance(FlexContext.getServletConfig(),this);
        } catch (ServletException e) {}
    }


	private AMFCaster getAMFCaster(ConfigMap properties) {

		Map amfCasterArguments=new HashMap();
		if(properties!=null){
			Cast caster = getEngine().getCastUtil();
			ConfigMap cases = properties.getPropertyAsMap("property-case", null);
	        if(cases!=null){
	        	if(!amfCasterArguments.containsKey("force-cfc-lowercase"))
	        		amfCasterArguments.put("force-cfc-lowercase",caster.toBoolean(cases.getPropertyAsBoolean("force-cfc-lowercase", false)));
	        	if(!amfCasterArguments.containsKey("force-query-lowercase"))
	        		amfCasterArguments.put("force-query-lowercase",caster.toBoolean(cases.getPropertyAsBoolean("force-query-lowercase", false)));
	        	if(!amfCasterArguments.containsKey("force-struct-lowercase"))
	        		amfCasterArguments.put("force-struct-lowercase",caster.toBoolean(cases.getPropertyAsBoolean("force-struct-lowercase", false)));

	        }
	        ConfigMap access = properties.getPropertyAsMap("access", null);
	        if(access!=null){
	        	if(!amfCasterArguments.containsKey("use-mappings"))
	        		amfCasterArguments.put("use-mappings",caster.toBoolean(access.getPropertyAsBoolean("use-mappings", false)));
	        	if(!amfCasterArguments.containsKey("method-access-level"))
	        		amfCasterArguments.put("method-access-level",access.getPropertyAsString("method-access-level","remote"));
	        }
		}

		AMFCaster amfCaster = new ClassicAMFCaster();
		amfCaster.init(amfCasterArguments);

		return amfCaster;
	}
}
