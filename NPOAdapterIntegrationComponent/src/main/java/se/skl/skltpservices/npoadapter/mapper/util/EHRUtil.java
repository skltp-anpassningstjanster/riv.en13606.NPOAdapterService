/**
 * Copyright (c) 2014 Inera AB, <http://inera.se/>
 *
 * This file is part of SKLTP.
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
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package se.skl.skltpservices.npoadapter.mapper.util;

import org.apache.commons.lang.StringUtils;

import riv.clinicalprocess.healthcond.description._2.ResultType;
import riv.clinicalprocess.healthcond.description.enums._2.ResultCodeEnum;
import se.rivta.en13606.ehrextract.v11.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public final class EHRUtil {

    private static ThreadLocal<SimpleDateFormat> formatter = new ThreadLocal<SimpleDateFormat>() {
        @Override
        public SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyyMMddHHmmss");
        }
    };

    //
    public static String formatTimestamp(Date timestamp) {
        return formatter.get().format(timestamp);
    }

	public static String getElementTextValue(final ELEMENT e) {
		if(e != null && e.getValue() instanceof ST) {
			ST text = (ST) e.getValue();
			return text.getValue();
		}
		return null;
	}
	
	public static ST stType(final String value) {
        if (value == null) {
            return null;
        }
        final ST st = new ST();
        st.setValue(value);
        return st;
    }
	
	public static TS tsType(final String value) {
		final TS ts = new TS();
		ts.setValue(value);
		return ts;
	}
		
	public static INT intType(final int value) {
		final INT _int = new INT();
		_int.setValue(value);
		return _int;
	}
	
	public static String getPartValue(final List<EN> names) {
        final EN item = firstItem(names);
        if (item != null) {
            final ENXP part = firstItem(item.getPart());
            return (part == null) ? null : part.getValue();
        }
        return null;
    }
	
    public static <T> T firstItem(final List<T> list) {
        return (list.size() == 0) ? null : list.get(0);
    }
    
    public static String getCDCode(final CD cd) {
        return (cd == null) ? null : cd.getCode();
    }
    
    public static IDENTIFIEDENTITY lookupDemographicIdentity(final List<IDENTIFIEDENTITY> demographics, final String hsaId) {
        for (final IDENTIFIEDENTITY identifiedentity : demographics) {
            if (hsaId.equals(identifiedentity.getExtractId().getExtension())) {
                return identifiedentity;
            }
        }
        return null;
    }
    
    public static ParameterType createParameter(String name, String value) {
        assert (name != null) && (value != null);
        final ParameterType parameterType = new ParameterType();
        parameterType.setName(stType(name));
        parameterType.setValue(stType(value));
        return parameterType;
    }
    
  	public static ELEMENT findEntryElement(final List<CONTENT> contents, final String type) {
  		for(CONTENT content : contents) {
  			if(content instanceof ENTRY) {
  				ENTRY e = (ENTRY) content;
  				for(ITEM item : e.getItems()) {
  					if(item instanceof ELEMENT) {
  						ELEMENT elm = (ELEMENT) item;
  						if(elm.getMeaning() != null && StringUtils.equals(elm.getMeaning().getCode(), type)) {
  							return elm;
  						}
  					}
  				}
  			}
  		}
  		return null;
  	}
  	
  	//TODO: Move to documenation-util
  	public static ResultCodeEnum interpret(final ResponseDetailTypeCodes code) {
		try {
			switch(code) {
			case E:
			case W:
				return ResultCodeEnum.ERROR;
			case I:
				return ResultCodeEnum.INFO;
			default:
				return ResultCodeEnum.OK;
			}
		} catch (Exception err) {
			return null;
		}
	}
  	
  	//TODO: Move to documentaiton-util
  	public static ResultType mapResultType(final String uniqueId, final List<ResponseDetailType> respDetails) {
		if(respDetails.isEmpty()) {
			return null;
		}
		final ResponseDetailType resp = respDetails.get(0);
		final ResultType resultType = new ResultType();
		if(resp.getText() != null) {
			resultType.setMessage(resp.getText().getValue());
		}
		resultType.setLogId(uniqueId);
		resultType.setResultCode(EHRUtil.interpret(resp.getTypeCode()));
		return resultType;
	}
  	
  	public static String getSystemHSAId(final EHREXTRACT ehrExtract) {
		if(ehrExtract.getEhrSystem() != null) {
			return ehrExtract.getEhrSystem().getExtension();
		}
		return null;
  	}
}
