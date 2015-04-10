package similarity

import org.apache.lucene.document.Document
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.IndexReader
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.Query
import org.apache.lucene.search.ScoreDoc
import org.apache.lucene.search.TopScoreDocCollector
import org.apache.lucene.queryparser.classic.QueryParser

class SearchManager {
    IndexManager indexManager
    IndexReader reader
    IndexSearcher searcher
    static final HITS_PER_PAGE = 10

    public SearchManager(IndexManager indexManager) {
        this.indexManager = indexManager
        reader = DirectoryReader.open(indexManager.indexDirectory)
        searcher = new IndexSearcher(reader)
    }

    ScoreDoc[] search(String querystr) {
        if(querystr == null || querystr.isEmpty()) return null

        Query query = new QueryParser("content", indexManager.analyzer).parse(querystr)
        //Prepare the result collection
        TopScoreDocCollector collector = TopScoreDocCollector.create(HITS_PER_PAGE)
        //Search
        searcher.search(query, collector)
        //Ranking
        ScoreDoc[] hits = collector.topDocs().scoreDocs
        println("Found " + hits.length + " hits.")
        hits?.eachWithIndex{ it, i ->
            Document d = searcher.doc(it.doc)
            println "${i+1}: ${d.get("content")}(${it.score})"
           //println searcher.explain(query, it.doc)
        }

        return hits
    }

    def displayResults(ScoreDoc[] hits) {
        println("Found " + hits.length + " hits.")
        hits?.eachWithIndex{ it, i ->
            Document d = searcher.doc(it.doc)
            println "${i+1}: ${d.get("content")}(${it.score})"
        }
        //reader.close()
    }

}
