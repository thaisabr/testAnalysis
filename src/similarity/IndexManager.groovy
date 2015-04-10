package similarity

import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.store.Directory
import org.apache.lucene.store.RAMDirectory
import scenarioParser.ParserGherkinJson


class IndexManager {

    StandardAnalyzer analyzer
    Directory indexDirectory
    IndexWriter writer

    public IndexManager() {
        analyzer = new StandardAnalyzer()
        //Create a memory directory; if necessary it is possible to define a index database
        indexDirectory = new RAMDirectory()
        //Create a file
        writer = new IndexWriter(indexDirectory, new IndexWriterConfig(analyzer))
    }

    private addDoc(String text){
        Document doc = new Document()
        doc.add(new Field("content", text, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.YES))
        writer.addDocument(doc)
    }

    def index(String featurePath, int scenarioLine) {
        def scenarioGherkin = ParserGherkinJson.getScenario(featurePath, scenarioLine)
        def text = ""

        /* se for considerar o termo dos steps (given, when, then) *
        scenarioGherkin.steps.each{
            text += it.keyword + it.name + "\n"
        }*/

        scenarioGherkin.steps.each{
            text += "${it.name}\n"
        }
        addDoc(text)
        writer.commit()
    }

    public void index(String featurePath) {
        def scenariosGherkin = ParserGherkinJson.getAllScenarios(featurePath)
        scenariosGherkin.each{ scenario ->
            def text = ""
            scenario.steps.each{
                text += it.keyword + it.name + "\n"
            }
            addDoc(text)
        }
        writer.commit()
    }

}
