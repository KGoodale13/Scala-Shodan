package com.kylegoodale.shodan.models

import play.api.libs.json._


object HostSearchResult {
  implicit val facetResultReads = Json.reads[FacetResult]
  implicit val hostSearchResultReads = Json.reads[HostSearchResult]
}

/**
  * Search result data returned from a api query
  * @param matches - List of devices that matched the search query
  * @param facets - Map of (facet ids -> List[Facet results]) for any requested facets.
  * @param total - The total number of results this query matched
  */
case class HostSearchResult(
  matches: List[Banner],
  facets: Option[Map[String, List[FacetResult]]],
  total: Int
)

/**
  * Container for a facet data point. A facet will will have multiple data points usually.
  * For example if you requested the facet country:10 (top 10 countries) the search result would have a list of country names and match counts
  * @param count - The number of results that matched the filter and also match this facet value
  * @param value - The value that describes what this facet is counting i.e "US" if the facet was "country"
  */
case class FacetResult(
  count: Int,
  value: String
)


