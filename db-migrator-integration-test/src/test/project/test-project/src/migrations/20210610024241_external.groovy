import freemarker.template.Configuration

def cfg = new Configuration(Configuration.VERSION_2_3_28)
cfg.setDefaultEncoding("UTF-8")
println "Freemarker template default encoding: " + cfg.getDefaultEncoding()


