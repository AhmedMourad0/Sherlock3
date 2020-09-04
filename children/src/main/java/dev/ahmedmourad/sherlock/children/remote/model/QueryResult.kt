package dev.ahmedmourad.sherlock.children.remote.model

import dev.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import dev.ahmedmourad.sherlock.domain.model.ids.ChildId

data class QueryResult(val id: QueryId, val childId: ChildId, val weight: Weight)
