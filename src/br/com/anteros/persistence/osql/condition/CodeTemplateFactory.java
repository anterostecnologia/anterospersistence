package br.com.anteros.persistence.osql.condition;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.com.anteros.persistence.osql.ConditionConverters;
import br.com.anteros.persistence.osql.condition.CodeTemplate.Element;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;


public class CodeTemplateFactory {

    private static final Pattern elementPattern = Pattern.compile("\\{%?%?\\d+[slu%]?%?\\}");

    public static final CodeTemplateFactory DEFAULT = new CodeTemplateFactory('\\');

    private final Map<String,CodeTemplate> cache = new ConcurrentHashMap<String,CodeTemplate>();

    private final ConditionConverters converters;
    
    public CodeTemplateFactory(char escape) {
        converters = new ConditionConverters(escape);
    }
    
    public CodeTemplate create(String template) {
        if (cache.containsKey(template)) {
            return cache.get(template);
        } else {
            Matcher m = elementPattern.matcher(template);
            final ImmutableList.Builder<Element> elements = ImmutableList.builder();
            int end = 0;
            while (m.find()) {
                if (m.start() > end) {
                    elements.add(new CodeTemplate.StaticText(template.substring(end, m.start())));
                }
                String str = template.substring(m.start() + 1, m.end() - 1).toLowerCase(Locale.ENGLISH);
                boolean asString = false;
                Function<Object, Object> transformer = null;
                if (str.charAt(0) == '%') {
                    if (str.charAt(1) == '%') {
                        transformer = converters.toEndsWithViaLikeLower;
                        str = str.substring(2);
                    } else {
                        transformer = converters.toEndsWithViaLike;
                        str = str.substring(1);
                    }

                }
                int strip = 0;
                switch (str.charAt(str.length()-1)) {
                case 'l' :
                    transformer = converters.toLowerCase;
                    strip = 1;
                    break;
                case 'u' :
                    transformer = converters.toUpperCase;
                    strip = 1;
                    break;
                case '%' :
                    if (transformer == null) {
                        if (str.charAt(str.length()-2) == '%') {
                            transformer = converters.toStartsWithViaLikeLower;
                            strip = 2;
                        } else {
                            transformer = converters.toStartsWithViaLike;
                            strip = 1;
                        }
                    } else {
                        if (str.charAt(str.length()-2) == '%') {
                            transformer = converters.toContainsViaLikeLower;
                            strip = 2;
                        } else {
                            transformer = converters.toContainsViaLike;
                            strip = 1;
                        }
                    }
                    break;
                case 's' :
                    asString = true;
                    strip = 1;
                    break;
                }
                if (strip > 0) {
                    str = str.substring(0, str.length()-strip);
                }
                int index = Integer.parseInt(str);
                if (asString) {
                    elements.add(new CodeTemplate.AsString(index));
                } else if (transformer != null) {
                    elements.add(new CodeTemplate.Transformed(index, transformer));
                } else {
                    elements.add(new CodeTemplate.ByIndex(index));
                }
                end = m.end();
            }
            if (end < template.length()) {
                elements.add(new CodeTemplate.StaticText(template.substring(end)));
            }
            CodeTemplate rv = new CodeTemplate(template, elements.build());
            cache.put(template, rv);
            return rv;
        }
    }

}
