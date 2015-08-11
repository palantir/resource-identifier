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

class EclipseCheckstylePlugin implements Plugin<Project> {
    void apply(Project project) {
        project.plugins.apply('eclipse')
        project.plugins.apply('checkstyle')
        project.tasks.create('eclipseCheckstyleConfig', EclipseCheckstyleConfigTask)
        project.tasks.eclipse.dependsOn project.tasks.eclipseCheckstyleConfig

        // use whenMerged so that checkstyle builder appears after the java builder
        project.eclipse.project.file.whenMerged { ep ->
            ep.natures.add 'net.sf.eclipsecs.core.CheckstyleNature'
            ep.buildCommands.add name: 'net.sf.eclipsecs.core.CheckstyleBuilder'
        }
    }
}

class EclipseCheckstyleConfigTask extends DefaultTask {
    @TaskAction
    void apply() {
        def writer = new File(project.projectDir, '.checkstyle').newWriter()
        def xml = new groovy.xml.MarkupBuilder(writer)
        xml.mkp.xmlDeclaration(version: '1.0', encoding: 'utf-8')
        xml.with {
            'fileset-config' ('file-format-version': '1.2.0', 'simple-config': 'true', 'sync-formatter': 'false') {
                'local-check-config' (name: 'Gradle Managed Checkstyle', location: project.checkstyle.configFile, type: 'external', description: 'Gradle Managed Checkstyle') {
                    'additional-data' (name: 'project-config-file', value: 'false')
                }
                fileset (name: 'all', enabled: 'true', 'check-config-name': 'Gradle Managed Checkstyle', local: 'true') {
                    'file-match-pattern' ('match-pattern': '.java', 'include-pattern': 'true')
                }
            }
        }
        writer.close()
    }
}

