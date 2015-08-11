// Copyright 2015 Palantir Technologies
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

class JavaVersionPlugin implements Plugin<Project> {
    void apply(Project gp) {
        JavaVersionExtension config = gp.extensions.create('javaVersion', JavaVersionExtension)
        gp.afterEvaluate {
            if (!config.version.equals('1.7') && !config.version.equals('1.8')) {
                throw new IllegalArgumentException("Valid Java versions are '1.7' or '1.8'")
            }
    
            String javaVersion = config.version
            String jdkVersion  = 'JDK_' + config.version.replaceAll('\\.', '_')
    
            gp.compileJava.sourceCompatibility = javaVersion
            gp.compileJava.targetCompatibility = javaVersion
    
            if (gp.plugins.findPlugin('eclipse')) {
                gp.eclipse {
                    jdt {
                        sourceCompatibility = javaVersion
                        targetCompatibility = javaVersion
                    }
                    classpath {
                        containers.clear()
                        containers.add('org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-'+javaVersion)
                    }
                }
            }
            if (gp.plugins.findPlugin('idea')) {
                gp.idea {
                    if (project != null) {
                        project {
                            languageLevel = javaVersion
                        }
                    }
                    module {
                        jdkName = javaVersion
                        iml {
                            withXml {
                                it.asNode().component.find { it.@name == 'NewModuleRootManager' }.@LANGUAGE_LEVEL = jdkVersion
                            }
                        }
                    }
                }
            }
        }
    }
}

class JavaVersionExtension {
    def String version = '1.8'    
}

