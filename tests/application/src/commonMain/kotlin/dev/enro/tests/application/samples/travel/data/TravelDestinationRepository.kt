package dev.enro.tests.application.samples.travel.data

import dev.enro.tests.application.samples.travel.domain.TravelDestination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TravelDestinationRepository private constructor() {
    private val _destinations = MutableStateFlow(sampleDestinations)
    val destinations: StateFlow<List<TravelDestination>> = _destinations.asStateFlow()

    fun getDestination(id: String): TravelDestination? {
        return _destinations.value.find { it.title == id }
    }

    fun searchDestinations(query: String): List<TravelDestination> {
        return _destinations.value.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.description.contains(query, ignoreCase = true)
        }
    }

    companion object {
        private val sampleDestinations = listOf(
            TravelDestination("🗼", "Paris", "City of lights and romance", "★★★★★"),
            TravelDestination("🏝️", "Bali", "Tropical paradise awaits", "★★★★☆"),
            TravelDestination("🗽", "New York", "The city that never sleeps", "★★★★★"),
            TravelDestination("🏔️", "Swiss Alps", "Mountain adventure calling", "★★★★☆"),
            TravelDestination("🏛️", "Rome", "Ancient history comes alive", "★★★★★"),
            TravelDestination("🌸", "Tokyo", "Modern meets traditional", "★★★★☆"),
            TravelDestination("🏖️", "Maldives", "Crystal clear waters", "★★★★★"),
            TravelDestination("🦁", "Safari Kenya", "Wildlife adventure", "★★★★☆"),
            TravelDestination("🌉", "San Francisco", "Golden Gate beauty", "★★★★☆"),
            TravelDestination("🏰", "Edinburgh", "Medieval charm", "★★★★☆"),
            TravelDestination("🎭", "Rio de Janeiro", "Carnival and beaches", "★★★★★"),
            TravelDestination("🕌", "Istanbul", "Where continents meet", "★★★★☆")
        )
        val instance = TravelDestinationRepository()
    }
}
