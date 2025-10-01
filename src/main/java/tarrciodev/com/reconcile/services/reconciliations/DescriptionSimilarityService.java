package tarrciodev.com.reconcile.services.reconciliations;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Service;

@Service
public class DescriptionSimilarityService {

    public double calculateSimilarity(String desc1, String desc2) {
        if (desc1 == null || desc2 == null) return 0.0;
        if (desc1.equalsIgnoreCase(desc2)) return 1.0;
        
        String normalized1 = normalizeDescription(desc1);
        String normalized2 = normalizeDescription(desc2);
        
        if (normalized1.isEmpty() || normalized2.isEmpty()) return 0.0;

        // Caso 1: Descrições idênticas após normalização
        if (normalized1.equals(normalized2)) {
            return 0.95;
        }

        // Caso 2: Uma descrição está contida na outra
        if (isOneContainedInOther(normalized1, normalized2)) {
            return 0.90;
        }

        // Caso 3: Para nomes próprios com pequenas diferenças
        if (isNameWithSmallDifference(normalized1, normalized2)) {
            return 0.85;
        }

        // Estratégias principais
        double tokenSimilarity = calculateEnhancedTokenSimilarity(normalized1, normalized2);
        double substringSimilarity = calculateEnhancedSubstringSimilarity(normalized1, normalized2);
        double sequenceSimilarity = calculateSequenceSimilarity(normalized1, normalized2);
        
        double totalSimilarity = 
            tokenSimilarity * 0.40 +
            substringSimilarity * 0.35 +
            sequenceSimilarity * 0.25;

        return Math.min(1.0, totalSimilarity);
    }

    private String normalizeDescription(String description) {
        if (description == null) return "";
        
        return description.toLowerCase()
            // Remover padrões específicos
            .replaceAll("tpa-\\d+[a-z]*\\s*", "")
            .replaceAll("ftm?\\d+[a-z/]*\\s*", "")
            .replaceAll("npc\\d+\\s*", "")
            .replaceAll("mv\\d+\\s*", "")
            .replaceAll("[^a-z0-9\\sáàâãéèêíïóôõöúçñ]", " ")
            // Corrigir problemas de acentuação e caracteres especiais
            .replace("á", "a").replace("à", "a").replace("â", "a").replace("ã", "a")
            .replace("é", "e").replace("è", "e").replace("ê", "e")
            .replace("í", "i").replace("ï", "i")
            .replace("ó", "o").replace("ô", "o").replace("õ", "o").replace("ö", "o")
            .replace("ú", "u")
            .replace("ç", "c")
            .replace("ñ", "n")
            // Remover prefixos
            .replaceAll("^(compras mn\\s*-?\\s*v?/?\\s*factura?\\s*-?\\s*|" +
                       "bancos?\\s*-?\\s*(depositos?|pag\\.?\\s*automaticos?)\\s*-?\\s*|" +
                       "pag|pgt|transferencia|deposito|debito|credito|" +
                       "serviço especial|tpa|ft|npc|stc|tc|fr|outros docs bancarios)\\s*", "")
            // Remover stop words
            .replaceAll("\\b(lda|ltda|sa|eireli|mei|para|de|do|da|em|com|por|os|as|no|na|" +
                       "pagamento|transferencia|deposito|debito|credito|servico|especial|" +
                       "grupo|group|compras|factura|v/?|ind|e|repr|com|geral|farmacia|popula)\\b", " ")
            .replaceAll("\\s+", " ")
            .trim();
    }

    private boolean isOneContainedInOther(String desc1, String desc2) {
        if (desc1.isEmpty() || desc2.isEmpty()) return false;
        
        // Casos como "superhello" vs "09 superhello" ou "cnd" vs "43 cnd"
        return (desc1.contains(desc2) && desc2.length() >= 2) || 
               (desc2.contains(desc1) && desc1.length() >= 2);
    }

    private boolean isNameWithSmallDifference(String desc1, String desc2) {
        // Para casos como "JOANIZETE SOLANGE FORTUNATO" vs "JOANIZETE SOLANGE FORTUNATO P"
        String[] words1 = desc1.split("\\s+");
        String[] words2 = desc2.split("\\s+");
        
        if (words1.length < 2 || words2.length < 2) return false;
        
        // Verificar se compartilham a maioria das palavras
        Set<String> set1 = new HashSet<>(Arrays.asList(words1));
        Set<String> set2 = new HashSet<>(Arrays.asList(words2));
        
        set1.retainAll(set2);
        int commonWords = set1.size();
        int totalWords = Math.max(words1.length, words2.length);
        
        return commonWords >= totalWords - 1; // Apenas 1 palavra de diferença
    }

    private double calculateEnhancedTokenSimilarity(String desc1, String desc2) {
        if (desc1.isEmpty() || desc2.isEmpty()) return 0.0;
        
        Set<String> tokens1 = new HashSet<>(Arrays.asList(desc1.split("\\s+")));
        Set<String> tokens2 = new HashSet<>(Arrays.asList(desc2.split("\\s+")));

        // Remover números isolados
        tokens1.removeIf(token -> token.matches("^\\d+$"));
        tokens2.removeIf(token -> token.matches("^\\d+$"));

        if (tokens1.isEmpty() && tokens2.isEmpty()) return 1.0;
        if (tokens1.isEmpty() || tokens2.isEmpty()) return 0.0;

        Set<String> intersection = new HashSet<>(tokens1);
        intersection.retainAll(tokens2);

        Set<String> union = new HashSet<>(tokens1);
        union.addAll(tokens2);

        double baseSimilarity = (double) intersection.size() / union.size();
        
        // Bonus para quando uma lista está contida na outra
        if (tokens1.containsAll(tokens2) || tokens2.containsAll(tokens1)) {
            return Math.max(baseSimilarity, 0.8);
        }
        
        return baseSimilarity;
    }

    private double calculateEnhancedSubstringSimilarity(String desc1, String desc2) {
        if (desc1.isEmpty() || desc2.isEmpty()) return 0.0;

        // Caso especial para strings muito similares
        if (isOneContainedInOther(desc1, desc2)) {
            String longer = desc1.length() > desc2.length() ? desc1 : desc2;
            String shorter = desc1.length() > desc2.length() ? desc2 : desc1;
            double containmentRatio = (double) shorter.length() / longer.length();
            return 0.7 + containmentRatio * 0.3; // Entre 0.7 e 1.0
        }

        // Procurar pela substring comum mais longa
        int maxLength = findLongestCommonSubstringLength(desc1, desc2);
        if (maxLength >= 3) {
            double maxPossible = Math.max(desc1.length(), desc2.length());
            return 0.3 + (maxLength / maxPossible) * 0.6;
        }

        return 0.0;
    }

    private int findLongestCommonSubstringLength(String s1, String s2) {
        int maxLength = 0;
        for (int i = 0; i < s1.length(); i++) {
            for (int j = 0; j < s2.length(); j++) {
                int length = 0;
                while (i + length < s1.length() && 
                       j + length < s2.length() && 
                       s1.charAt(i + length) == s2.charAt(j + length)) {
                    length++;
                }
                if (length > maxLength) {
                    maxLength = length;
                }
            }
        }
        return maxLength;
    }

    private double calculateSequenceSimilarity(String desc1, String desc2) {
        String[] words1 = desc1.split("\\s+");
        String[] words2 = desc2.split("\\s+");
        
        if (words1.length == 0 || words2.length == 0) return 0.0;
        
        int matches = 0;
        for (String word1 : words1) {
            if (word1.length() < 2) continue;
            for (String word2 : words2) {
                if (word2.length() < 2) continue;
                if (word1.equals(word2) || 
                    word1.contains(word2) || 
                    word2.contains(word1) ||
                    calculateLevenshteinSimilarity(word1, word2) > 0.7) {
                    matches++;
                    break;
                }
            }
        }
        
        int totalMeaningful = (int) Arrays.stream(words1).filter(w -> w.length() >= 2).count() +
                            (int) Arrays.stream(words2).filter(w -> w.length() >= 2).count();
        
        return totalMeaningful > 0 ? (double) matches * 2 / totalMeaningful : 0.0;
    }

    private double calculateLevenshteinSimilarity(String s1, String s2) {
        int maxLength = Math.max(s1.length(), s2.length());
        if (maxLength == 0) return 1.0;
        
        int distance = calculateLevenshteinDistance(s1, s2);
        return 1.0 - (double) distance / maxLength;
    }

    private int calculateLevenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = Math.min(
                        Math.min(dp[i-1][j] + 1, dp[i][j-1] + 1),
                        dp[i-1][j-1] + (s1.charAt(i-1) == s2.charAt(j-1) ? 0 : 1)
                    );
                }
            }
        }
        
        return dp[s1.length()][s2.length()];
    }

    public void printSimilarityAnalysis(String desc1, String desc2) {
        double similarity = calculateSimilarity(desc1, desc2);
        System.out.printf("Descrição 1: %s%n", desc1);
        System.out.printf("Descrição 2: %s%n", desc2);
        System.out.printf("Similaridade total: %.2f%n", similarity);
        
        String norm1 = normalizeDescription(desc1);
        String norm2 = normalizeDescription(desc2);
        System.out.printf("Normalizado 1: %s%n", norm1);
        System.out.printf("Normalizado 2: %s%n", norm2);
        
        if (similarity > 0.6) {
            System.out.println("✅ MATCH POTENCIAL");
        } else if (similarity > 0.4) {
            System.out.println("⚠️  MATCH FRACO");
        }
        System.out.println("---");
    }
}
