package similarity

import org.apache.commons.math3.linear.ArrayRealVector
import org.apache.commons.math3.linear.RealVector
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.IndexReader
import org.apache.lucene.index.TermsEnum
import org.apache.lucene.util.BytesRef
import org.apache.lucene.index.Terms


class ScenarioSimilarityAnalyser {

    IndexManager indexManager
    IndexReader reader
    Set terms

    private configureIndexManager(String path1, int line1, String path2, int line2){
        indexManager = new IndexManager()
        indexManager.index(path1, line1)
        indexManager.index(path2, line2)
    }

    double calculateSimilarity(String path1, int line1, String path2, int line2){
        terms = [] as Set
        configureIndexManager(path1, line1, path2, line2)
        reader = DirectoryReader.open(indexManager.indexDirectory)

        def freqVectorScenario1 = getTermFrequencies(0).sort()
        println "vector1: $freqVectorScenario1"
        def freqVectorScenario2 = getTermFrequencies(1).sort()
        println "vector2: $freqVectorScenario2"

        RealVector v1 = toRealVector(freqVectorScenario1)
        RealVector v2 = toRealVector(freqVectorScenario2)

        getCosineSimilarity(v1, v2)
    }

    private getTermFrequencies(int docId){
        Terms vector = reader.getTermVector(docId, "content")
        TermsEnum termsEnum = null
        termsEnum = vector.iterator(termsEnum)

        def frequencies = [:]
        BytesRef text
        while ((text = termsEnum.next()) != null) {
            String term = text.utf8ToString()
            int freq = (int) termsEnum.totalTermFreq()
            frequencies += [(term):freq]
            terms += term
        }
        frequencies
    }

    private RealVector toRealVector(Map map) {
        RealVector vector = new ArrayRealVector(terms.size())
        int i = 0
        terms.each{ term ->
            int value = map.containsKey(term) ? map.get(term) : 0
            vector.setEntry(i++, value)
        }
        return (RealVector) vector.mapDivide(vector.getL1Norm())
    }

    private static double getCosineSimilarity(RealVector v1, RealVector v2){
        v1.dotProduct(v2) / (v1.norm * v2.norm)
    }

}
