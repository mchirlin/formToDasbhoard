package com.appiancorp.ps.plugins.systemtools;

import com.appiancorp.ix.AbstractTypeMap;
import com.appiancorp.ix.Type;
import com.appiancorp.ix.binding.Binding;
import com.appiancorp.ix.binding.ExportBinderMap;
import com.appiancorp.ix.binding.ImportBinderMap;
import com.appiancorp.ix.binding.UnresolvedException;

public class TypeHelper {
	public static Object getType(ImportBinderMap ibm, String typeKey, String uuid) throws UnresolvedException {
		return getType(ibm, typeKey).bind(uuid);
	}

	public static Object getType(ExportBinderMap ebm, String typeKey, Long id) throws UnresolvedException {
		return getType(ebm, typeKey).bind(id);
	}

	public static Binding<Object, ?> getType(AbstractTypeMap<Binding<?,?>> ebm, String typeKey) {
		return (Binding<Object, ?>) ebm.get(Type.get(typeKey));
	}
}
