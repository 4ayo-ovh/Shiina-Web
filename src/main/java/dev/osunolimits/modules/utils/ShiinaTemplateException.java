package dev.osunolimits.modules.utils;

import java.io.Writer;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import freemarker.core.Environment;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

public class ShiinaTemplateException implements TemplateExceptionHandler{

     private final Logger LOG = (Logger) LoggerFactory.getLogger(ShiinaTemplateException.class);

    @Override
    public void handleTemplateException(TemplateException te, Environment env, Writer out) throws TemplateException {
        LOG.error("Error processing template " + te.getTemplateSourceName() + " | " +  te.getMessage());
    }
    
}
