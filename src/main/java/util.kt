fun <F, S, R> Iterable<Pair<F, S>>.mapFirst(transform: (F) -> R) = map { (f, s) -> transform(f) to s }

fun <F, S, R> Iterable<Pair<F, S>>.mapSecond(transform: (S) -> R) = map { (f, s) -> f to transform(s) }
