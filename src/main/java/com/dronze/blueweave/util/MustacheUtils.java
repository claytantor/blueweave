package com.dronze.blueweave.util;


import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.apache.commons.compress.utils.IOUtils;

import java.io.*;
import java.util.Map;
import java.util.UUID;

/**
 * Created by claytongraham on 3/25/16.
 */
public class MustacheUtils {

    public static String merge(String template, Map<String,Object> model){
        Writer w = new StringWriter();
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile(new StringReader(template), UUID.randomUUID().toString());
        mustache.execute(w,model);
        return w.toString();
    }

    public static String mergeClaspath(String classPath, Map<String,Object> model) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOUtils.copy(MustacheUtils.class.getResourceAsStream(classPath), baos);
        return MustacheUtils.merge(baos.toString(),model);

    }
}
